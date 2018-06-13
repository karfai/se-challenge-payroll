// Copyright 2018 Don Kelly <karfai@gmail.com>

// Licensed under the Apache License, Version 2.0 (the "License"); you
// may not use this file except in compliance with the License. You may
// obtain a copy of the License at

// http://www.apache.org/licenses/LICENSE-2.0

// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
// implied. See the License for the specific language governing
// permissions and limitations under the License.

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
