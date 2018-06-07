package models

import java.sql.Date
import java.text.SimpleDateFormat
import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.{ Future, ExecutionContext }
import slick.jdbc.JdbcProfile

case class Hour(time_sheet_id: String, date: Date, hours: Double, employee_id: Int, group_id: String)

@Singleton
class HoursRepository @Inject() (db_cfg_provider: DatabaseConfigProvider)(implicit ctx: ExecutionContext) {
  private val _cfg = db_cfg_provider.get[JdbcProfile]

  import _cfg._
  import profile.api._

  private class HoursTable(tag: Tag) extends Table[Hour](tag, "hours") {
    def time_sheet_id = column[String]("time_sheet_id")
    def date          = column[Date]("date")
    def hours         = column[Double]("hours")
    def employee_id   = column[Int]("employee_id")
    def group_id      = column[String]("group_id")

    def * = (time_sheet_id, date, hours, employee_id, group_id) <> ((Hour.apply _).tupled, Hour.unapply)
  }

  private val _hours = TableQuery[HoursTable]
  private val _format = new SimpleDateFormat("dd/MM/yyyy")

  def add_many(time_sheet_id: String, entries: Seq[Tuple4[String, Double, Int, String]]): Future[Option[Int]] = db.run {
    _hours ++= entries.map { tup =>
      Hour(time_sheet_id, new Date(_format.parse(tup._1).getTime()), tup._2, tup._3, tup._4)
    }
//    _hours.length.result
  }

  def sheet_exists(id: String): Future[Boolean] = db.run {
    _hours.filter(_.time_sheet_id === id).exists.result
  }
}
