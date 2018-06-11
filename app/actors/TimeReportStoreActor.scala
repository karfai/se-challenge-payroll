package actors

import javax.inject._

import akka.actor._
import play.api.Logger
import scala.util.{ Failure, Success }

import models.{ HoursRepository }

import scala.concurrent.ExecutionContext.Implicits.global

class TimeReportStoreActor @Inject() (
  hours_repo: HoursRepository
) extends Actor {
  def receive: Receive = {
    case GlobalMessages.ReportReady(report) => {
      Logger.info("storing new report")
      hours_repo.add_many(
        report.id, report.hours.map { hw => Tuple4(hw.date, hw.hours, hw.employee_id, hw.group) }
      )
    }
  }
}
