package models

case class EmployeePay(id: Long, name: String, groups: Set[String], total: Double)
case class EmployeePayPeriod(pay: EmployeePay, starts: String = null, ends: String = null)

