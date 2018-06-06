package models

case class Employee(id: Long, name: String, group_id: Option[String])

import play.api.libs.json._

object Employee {
  implicit val fmt = Json.format[Employee]
}
