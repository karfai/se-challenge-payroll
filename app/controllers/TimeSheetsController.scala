package controllers

import java.io.File
import java.nio.file.{ Files, Path }
import javax.inject._
import collection.JavaConverters._

import akka.actor.{ ActorRef }
import akka.pattern.ask
import akka.stream.IOResult
import akka.stream.scaladsl._
import akka.util.{ ByteString, Timeout }
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.streams._
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc._
import play.core.parsers.Multipart.FileInfo
import scala.concurrent.Promise
import scala.concurrent.duration._
import scala.util.{ Success, Failure }

import actors.{ Messages }
import models.{ EmployeePay, EmployeePayPeriod }

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class TimeSheetsController @Inject()(
  @Named("actors-parse-report") actor_parse_report: ActorRef,
  @Named("actors-payroll-report") actor_payroll_report: ActorRef,
  cc: ControllerComponents
) extends AbstractController(cc) {
  type FilePartHandler[A] = (FileInfo) => Accumulator[ByteString, FilePart[A]]

  private def handleFilePartAsFile: FilePartHandler[File] = {
    case FileInfo(pn, fn, ct) => {
      val path = Files.createTempFile("multipartBody", "tempFile")
      val sink = FileIO.toPath(path)
      val acc = Accumulator(sink)
      acc.map {
        case IOResult(count, status) => {
          FilePart(pn, fn, ct, path.toFile)
        }
      }
    }
  }

  implicit val timeout: Timeout = 5.seconds

  def create() = Action.async(parse.multipartFormData(handleFilePartAsFile)) { implicit request =>
    val pr = Promise[Result]()

    request.body.file("content").map {
      case FilePart(path, file_name, content_type, file) => {
        (actor_parse_report ? Messages.TimeSheetReceived(file)).onComplete {
          case Success(m) => {
            m match {
              case Messages.ReportProcessed(exists, id) => {
                if (exists) {
                  pr.success(
                    Forbidden(
                      Json.obj(
                        "status"  -> "report_exists",
                        "message" -> s"Report exists (id=${id})",
                        "args"    -> Map("id" -> id)))
                  )
                } else {
                  pr.success(
                    Ok(Json.obj("status" -> "ok", "args" -> Map("id" -> id)))
                  )
                }
              }

              case Messages.ReportParseFailed() => {
                pr.success(
                  InternalServerError(
                    Json.obj(
                      "status"  -> "failure_report_parse",
                      "message" -> s"Failed to parse the uploaded file"))
                )
              }

              case Messages.DatabaseFailure(th) => {
                pr.success(
                  InternalServerError(Json.obj(
                    "status"  -> "failure_database",
                    "message" -> s"Failed to connect to database"))
                )
              }
            }
          }

          case Failure(e) => println(e)
        }
      }
    }

    pr.future
  }

  implicit val employee_pay_writes: Writes[EmployeePay] = (
    (JsPath \ "id").write[Long] and
    (JsPath \ "name").write[String] and
    (JsPath \ "groups").write[Set[String]] and
    (JsPath \ "total").write[Double]
  )(unlift(EmployeePay.unapply))

  implicit val employee_pay_period_writes: Writes[EmployeePayPeriod] = (
    (JsPath \ "pay").write[EmployeePay] and
    (JsPath \ "starts").write[String] and
    (JsPath \ "ends").write[String]
  )(unlift(EmployeePayPeriod.unapply))

  def periods() = Action.async { implicit result =>
    val pr = Promise[Result]()
    (actor_payroll_report ? Messages.PayrollReportRequested()).onComplete {
      case Success(m) => m match {
        case Messages.PayrollReportCompleted(report) => {
          pr.success(
            Ok(
              Json.obj("status" -> "ok", "report" -> report))
          )
        }

        case Messages.PayrollReportUnavailable() => {
          pr.success(
            InternalServerError(
              Json.obj("status" -> "failure_report_unavailable"))
          )
        }
      }

      case Failure(th) => pr.success(
        InternalServerError(
          Json.obj("status" -> "failure_produce_report"))
      )
    }

    pr.future
  }
}
