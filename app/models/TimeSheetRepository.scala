package models

import com.github.nscala_time.time.Imports._
import java.sql.Date
import java.text.SimpleDateFormat
import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json._
import scala.concurrent.{ Future, ExecutionContext }
import slick.jdbc.JdbcProfile

@Singleton
class TimeSheetsRepository @Inject() (db_cfg_provider: DatabaseConfigProvider)(implicit ctx: ExecutionContext) {
  private val _cfg = db_cfg_provider.get[JdbcProfile]

  import _cfg._
  import profile.api._

  private val _hours = TableQuery[HoursTable]
  private val _employees = TableQuery[EmployeesTable]
  private val _groups = TableQuery[GroupsTable]

  def sheet_exists(id: String): Future[Boolean] = db.run {
    _hours.filter(_.time_sheet_id === id).exists.result
  }

  def report = db.run {
    _hours.join(_employees).on(_.employee_id === _.id).join(_groups).on(_._1.group_id === _.name).map { tup =>
      // < ((Hour, Employee), Group)
      // > (employee.id, employee.name, hours.period, hours.hours, group.name, group.rate)
      (tup._1._2.id, tup._1._2.name, tup._1._1.period, tup._1._1.hours, tup._2.name, tup._2.rate)
    }.result
  }
}
