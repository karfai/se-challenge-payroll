package actors

import javax.inject._

import akka.actor._
import com.github.nscala_time.time.Imports._
import play.api.Logger
import scala.concurrent.{ Future, Promise }
import scala.util.{ Failure, Success }

import models.{ HoursRepository, EmployeePay, EmployeePayPeriod }

import scala.concurrent.ExecutionContext.Implicits.global

class PayrollReportActor @Inject() (
  hours_repo: HoursRepository
) extends Actor {
  private def calculate_report(): Future[Seq[EmployeePayPeriod]] = {
    val pr = Promise[Seq[EmployeePayPeriod]]()

    Logger.info("regenerating the report")
    
    hours_repo.summarize.onComplete {
      case Success(summary) => {
        // > (employee.id, employee.name, hours.period, hours.hours, group.name, group.rate)
        // < Map[hours.period -> Map[employee.id -> EmployeePay]]
        val totals = summary.foldLeft(Map[String, Map[Long, EmployeePay]]()) { (m, tup) =>
          val employees = m.getOrElse(tup._3, Map[Long, EmployeePay]())
          val emp_pay = employees.get(tup._1) match {
            case None => EmployeePay(tup._1, tup._2, Set(tup._5), tup._4 * tup._6)
            case Some(ep) => EmployeePay(tup._1, tup._2, ep.groups ++ Set(tup._5), ep.total + tup._4 * tup._6)
          }

          m ++ Map(tup._3 -> (employees ++ Map(tup._1 -> emp_pay)))
        }

       val report = totals.keySet.toSeq.sorted.map { period =>
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

        pr.success(report)
      }

      case Failure(e) => {
        pr.failure(e)
      }
    }

    pr.future
  }

  def receive: Receive = {
    case GlobalMessages.ReportReady(report) => {
      Logger.info("new report pushed")
    }

    case Messages.PayrollReportRequested() => {
      val them = sender()

      calculate_report.onComplete {
        case Success(report) => them ! Messages.PayrollReportCompleted(report)
        case Failure(e) => them ! Messages.PayrollReportUnavailable()
      }
    }
  }
}
