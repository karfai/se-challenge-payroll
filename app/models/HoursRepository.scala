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
class HoursRepository @Inject() (db_cfg_provider: DatabaseConfigProvider)(implicit ctx: ExecutionContext) {
  private val _cfg = db_cfg_provider.get[JdbcProfile]

  import _cfg._
  import profile.api._

  private val _hours = TableQuery[HoursTable]
  private val _format = DateTimeFormat.forPattern("dd/MM/yyyy")

  def add_many(time_sheet_id: String, entries: Seq[Tuple4[String, Double, Int, String]]): Future[Option[Int]] = db.run {
    _hours ++= entries.map { tup =>
      val dt = _format.parseDateTime(tup._1)
      val partition = 2 * dt.getMonthOfYear() + (dt.getDayOfMonth() / 15)
      val period = f"${dt.getYear()}-$partition%02d"
      Hour(time_sheet_id, new Date(dt.getMillis()), tup._2, tup._3, tup._4, period)
    }
  }

  def sheet_exists(id: String): Future[Boolean] = db.run {
    _hours.filter(_.time_sheet_id === id).exists.result
  }

  def all: Future[Seq[Hour]] = db.run {
    _hours.result
  }
}
