package models

import java.io.File

import com.github.nscala_time.time.Imports._
import com.github.tototoshi.csv._

case class HoursWorked(date: DateTime, hours: Double, employee_id: Int, group: String)
case class TimeReport(id: String, hours: Seq[HoursWorked])

object ParseTimeReport {
  private val _format = DateTimeFormat.forPattern("dd/MM/yyyy")

  def apply(file: File): Option[TimeReport] = {
    val rdr = CSVReader.open(file)
    rdr.allWithHeaders().reverse match {
      case (line: Map[String, String]) :: tail => {
        val report_id = line("hours worked")
        val hours_worked = tail.map { ln =>
          HoursWorked(
            _format.parseDateTime(ln("date")),
            ln("hours worked").toDouble,
            ln("employee id").toInt,
            ln("job group"))
        }

        Some(TimeReport(report_id, hours_worked))
      }

      case _ => None
    }
  }
}
