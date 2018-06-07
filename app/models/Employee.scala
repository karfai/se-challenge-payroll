package models

import slick.lifted.Tag
import slick.jdbc.PostgresProfile.api._

case class Employee(id: Long, name: String, group_id: Option[String])

class EmployeesTable(tag: Tag) extends Table[Employee](tag, "employees") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def group_id = column[Option[String]]("group_id")

  def * = (id, name, group_id) <> ((Employee.apply _).tupled, Employee.unapply)
}
