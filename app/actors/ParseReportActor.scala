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

package actors

import javax.inject._

import akka.actor._
import scala.util.{ Failure, Success }

import models.{ ParseTimeReport, ReportsRepository }

import scala.concurrent.ExecutionContext.Implicits.global

class ParseReportActor @Inject() (
  ges: services.GlobalEventStream,
  reports_repo: ReportsRepository
) extends Actor {
  def receive: Receive = {
    case Messages.TimeSheetReceived(file) => {
      val them = sender()
      ParseTimeReport(file) match {
        case Some(report) => {
          reports_repo.exists(report.id).onComplete {
            case Success(exists) => {
              them ! Messages.ReportProcessed(exists, report.id)
              if (!exists) {
                ges.publish(GlobalMessages.ReportReady(report))
              }
            }

            case Failure(e) => {
              them ! Messages.DatabaseFailure(e)
            }
          }
        }

        case None => {
          them ! Messages.ReportParseFailed()
        }
      }
    }
  }
}
