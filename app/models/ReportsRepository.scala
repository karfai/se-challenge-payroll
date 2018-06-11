package models

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.{ Future, ExecutionContext }
import slick.jdbc.JdbcProfile

@Singleton
class ReportsRepository @Inject() (db_cfg_provider: DatabaseConfigProvider)(implicit ctx: ExecutionContext) {
  private val _cfg = db_cfg_provider.get[JdbcProfile]

  import _cfg._
  import profile.api._

  private val _hours = TableQuery[HoursTable]

  def exists(id: String): Future[Boolean] = db.run {
    _hours.filter(_.time_sheet_id === id).exists.result
  }
}
