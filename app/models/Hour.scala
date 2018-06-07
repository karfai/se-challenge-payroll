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
