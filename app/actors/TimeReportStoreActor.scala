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
import play.api.Logger
import scala.util.{ Failure, Success }

import models.{ HoursRepository }

import scala.concurrent.ExecutionContext.Implicits.global

class TimeReportStoreActor @Inject() (
  hours_repo: HoursRepository
) extends Actor {
  def receive: Receive = {
    case GlobalMessages.ReportReady(report) => {
      Logger.info("storing new report")
      hours_repo.add_many(
        report.id, report.hours.map { hw => Tuple4(hw.date, hw.hours, hw.employee_id, hw.group) }
      )
    }
  }
}
