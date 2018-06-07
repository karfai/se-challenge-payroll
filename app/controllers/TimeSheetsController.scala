package controllers

import java.io.File
import java.nio.file.{Files, Path}
import javax.inject._
import collection.JavaConverters._

import akka.stream.IOResult
import akka.stream.scaladsl._
import akka.util.ByteString
import com.github.nscala_time.time.Imports._
import com.github.tototoshi.csv._
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
import scala.util.{ Success, Failure }

import models.{ HoursRepository, TimeSheetsRepository }

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class TimeSheetsController @Inject()(cc: ControllerComponents, hours_repo: HoursRepository, time_sheets_repo: TimeSheetsRepository) extends AbstractController(cc) {
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

  def create() = Action.async(parse.multipartFormData(handleFilePartAsFile)) { implicit request =>
    val pr = Promise[Result]()
    val fileOption = request.body.file("content").map {
      case FilePart(k, fn, ct, file) => {
        val rdr = CSVReader.open(file)
        rdr.allWithHeaders().reverse match {
          case (report: Map[String, String]) :: tail => {
            val report_id = report("hours worked")
            time_sheets_repo.sheet_exists(report_id).onComplete {
              case Success(exists) => {
                if (!exists) {
                  val entries = tail.map { ln => Tuple4(ln("date"), ln("hours worked").toDouble, ln("employee id").toInt, ln("job group")) }
                  hours_repo.add_many(report_id, entries).onComplete {
                    case Success(len) => {
                      pr.success(Ok(Json.obj("status" -> "ok")))
                    }
                    case Failure(e) => {
                      println(e)
                      pr.success(InternalServerError(Json.obj("status" -> "insert_failure")))
                    }
                  }
                } else {
                  pr.success(Forbidden(Json.obj("status" -> "report_exists", "args" -> Map("id" -> report_id), "message" -> s"Report exists (id=${report_id})")))
                }
              }
              case Failure(e) => pr.success(InternalServerError(Json.obj("status" -> "database_failure")))
            }
          }
          case _ => pr.success(InternalServerError(Json.obj("status" -> "parse_failure")))
        }

      }

      case _ => pr.success(InternalServerError(Json.obj("status" -> "failure", "message" -> "data not received")))
    }

    pr.future
  }

  case class EmployeePay(id: Long, name: String, groups: Set[String], total: Double)
  case class EmployeePayPeriod(pay: EmployeePay, starts: String = null, ends: String = null)

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
    time_sheets_repo.report.onComplete {
      case Success(report) => {
        // > (employee.id, employee.name, hours.period, hours.hours, group.name, group.rate)
        // < Map[hours.period -> Map[employee.id -> EmployeePay]]
        val totals = report.foldLeft(Map[String, Map[Long, EmployeePay]]()) { (m, tup) =>
          val employees = m.getOrElse(tup._3, Map[Long, EmployeePay]())
          val emp_pay = employees.get(tup._1) match {
            case None => EmployeePay(tup._1, tup._2, Set(tup._5), tup._4 * tup._6)
            case Some(ep) => EmployeePay(tup._1, tup._2, ep.groups ++ Set(tup._5), ep.total + tup._4 * tup._6)
          }

          m ++ Map(tup._3 -> (employees ++ Map(tup._1 -> emp_pay)))
        }

        val sorted = totals.keySet.toSeq.sorted.map { period =>
          val employees: Map[Long, EmployeePay] = totals(period)
          employees.values.toSeq.sortWith { _.name < _.name }.map { ep =>
            period.split("-") match {
              case Array(year, part) => {
                val yi = year.toInt
                val i = part.toInt
                val month = i / 2
                val dates = (i - month * 2) match {
                  case 0 => (new DateTime(yi, month, 1, 0, 0, 0, 0), new DateTime(yi, month, 15, 0, 0, 0, 0))
                  case 1 => {
                    val start = new DateTime(yi, month, 16, 0, 0, 0, 0)
                    (start, new DateTime(yi, month, start.dayOfMonth().getMaximumValue(), 0, 0, 0, 0))
                  }
                }

                EmployeePayPeriod(ep, dates._1.toString("dd/MM/yyyy"), dates._2.toString("dd/MM/yyyy"))
              }
              case _ => EmployeePayPeriod(ep)
            }
          }
        }.flatten

        pr.success(Ok(Json.obj("status" -> "ok", "totals" -> sorted)))
      }
      case Failure(e) => {
        println(e)
        pr.success(Ok(""))
      }
    }
    pr.future
  }
}
