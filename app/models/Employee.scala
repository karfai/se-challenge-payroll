// Copyright 2018 Don Kelly <karfai@gmail.com>

// Licensed under the Apache License, Version 2.0 (the "License"); you
// may not use this file except in compliance with the License. You may
// obtain a copy of the License at

// http://www.apache.org/licenses/LICENSE-2.0

// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
// implied. See the License for the specific language governing
// permissions and limitations under the License.

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
