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

import java.sql.{ Date => SqlDate }
import slick.lifted.Tag
import slick.jdbc.PostgresProfile.api._

case class Hour(time_sheet_id: String, date: SqlDate, hours: Double, employee_id: Long, group_id: String, period: String)

class HoursTable(tag: Tag) extends Table[Hour](tag, "hours") {
  def time_sheet_id = column[String]("time_sheet_id")
  def date          = column[SqlDate]("date")
  def hours         = column[Double]("hours")
  def employee_id   = column[Long]("employee_id")
  def group_id      = column[String]("group_id")
  def period        = column[String]("period")

  def * = (time_sheet_id, date, hours, employee_id, group_id, period) <> ((Hour.apply _).tupled, Hour.unapply)
}
