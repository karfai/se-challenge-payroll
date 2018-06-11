package actors

import javax.inject._

import akka.actor._
import scala.util.{ Failure, Success }

import models.{ ParseTimeReport, ReportsRepository }

import scala.concurrent.ExecutionContext.Implicits.global

class ParseReportActor @Inject() (
  ges: services.GlobalEventStream,
  reports_repo: ReportsRepository
) extends Actor {
  def receive: Receive = {
    case Messages.TimeSheetReceived(file) => {
      val them = sender()
      ParseTimeReport(file) match {
        case Some(report) => {
          reports_repo.exists(report.id).onComplete {
            case Success(exists) => {
              them ! Messages.ReportProcessed(exists, report.id)
              if (!exists) {
                ges.publish(GlobalMessages.ReportReady(report))
              }
            }

            case Failure(e) => {
              them ! Messages.DatabaseFailure(e)
            }
          }
        }

        case None => {
          them ! Messages.ReportParseFailed()
        }
      }
    }
  }
}
