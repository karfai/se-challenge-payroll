package models

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.{ Future, ExecutionContext }
import slick.jdbc.JdbcProfile

@Singleton
class EmployeesRepository @Inject() (provider: DatabaseConfigProvider)(implicit ctx: ExecutionContext) {
  private val _cfg = provider.get[JdbcProfile]

  import _cfg._
  import profile.api._

  private class EmployeesTable(tag: Tag) extends Table[Employee](tag, "employees") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def group_id = column[Option[String]]("group_id")

    def * = (id, name, group_id) <> ((Employee.apply _).tupled, Employee.unapply)
  }

  private val _employees = TableQuery[EmployeesTable]

  def list(): Future[Seq[Employee]] = db.run {
    _employees.result
  }
}
