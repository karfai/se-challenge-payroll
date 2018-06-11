package models

import com.github.nscala_time.time.Imports._
import java.sql.Date
import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json._
import scala.concurrent.{ Future, ExecutionContext }
import slick.jdbc.JdbcProfile

@Singleton
class HoursRepository @Inject() (db_cfg_provider: DatabaseConfigProvider)(implicit ctx: ExecutionContext) {
  private val _cfg = db_cfg_provider.get[JdbcProfile]

  import _cfg._
  import profile.api._

  private val _hours = TableQuery[HoursTable]
  private val _employees = TableQuery[EmployeesTable]
  private val _groups = TableQuery[GroupsTable]

  def add_many(time_sheet_id: String, entries: Seq[Tuple4[DateTime, Double, Int, String]]): Future[Option[Int]] = db.run {
    _hours ++= entries.map { tup =>
      val partition = 2 * tup._1.getMonthOfYear() + (tup._1.getDayOfMonth() / 15)
      val period = f"${tup._1.getYear()}-$partition%02d"
      Hour(time_sheet_id, new Date(tup._1.getMillis()), tup._2, tup._3, tup._4, period)
    }
  }

  def summarize = db.run {
    _hours.join(_employees).on(_.employee_id === _.id).join(_groups).on(_._1.group_id === _.name).map { tup =>
      // < ((Hour, Employee), Group)
      // > (employee.id, employee.name, hours.period, hours.hours, group.name, group.rate)
      (tup._1._2.id, tup._1._2.name, tup._1._1.period, tup._1._1.hours, tup._2.name, tup._2.rate)
    }.result
  }
}
