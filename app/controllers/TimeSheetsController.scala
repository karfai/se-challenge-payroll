package controllers

import java.io.File
import java.nio.file.{Files, Path}
import javax.inject._

import akka.stream.IOResult
import akka.stream.scaladsl._
import akka.util.ByteString
import com.github.tototoshi.csv._
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json._
import play.api.libs.streams._
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc._
import play.core.parsers.Multipart.FileInfo
import scala.concurrent.Promise
import scala.util.{ Success, Failure }

import models.{ HoursRepository }

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class TimeSheetsController @Inject()(cc: ControllerComponents, hours_repo: HoursRepository) extends AbstractController(cc) {
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
            hours_repo.sheet_exists(report_id).onComplete {
              case Success(exists) => {
                if (!exists) {
                  val entries = tail.map { ln => Tuple4(ln("date"), ln("hours worked").toDouble, ln("employee id").toInt, ln("job group")) }
                  hours_repo.add_many(report_id, entries).onComplete {
                    case Success(len) => {
                      println(len)
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
}
