package models

import slick.lifted.Tag
import slick.jdbc.PostgresProfile.api._

case class Group(name: String, rate: Double)

class GroupsTable(tag: Tag) extends Table[Group](tag, "groups") {
  def name = column[String]("name")
  def rate = column[Double]("rate")

  def * = (name, rate) <> ((Group.apply _).tupled, Group.unapply)
}
