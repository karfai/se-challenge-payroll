package controllers

import javax.inject._
import play.api._
import play.api.libs.json._
import play.api.mvc._
import scala.concurrent.{ Future }
import scala.util.{ Success, Failure }

import models.{ EmployeesRepository, HoursRepository }

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class ReportsController @Inject()(cc: ControllerComponents, hours_repo: HoursRepository, employees_repo: EmployeesRepository) extends AbstractController(cc) {
  def index() = Action.async { request =>
    // lists all of the pay periods in the DB
    employees_repo.list().map { employees =>
      Ok(Json.obj("status" -> "ok", "employees" -> employees))
    }
  }
}
