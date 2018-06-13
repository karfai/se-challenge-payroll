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

import java.io.File
import models.{ TimeReport, EmployeePayPeriod }

object Messages {
  case class TimeSheetReceived(file: File)
  case class PayrollReportRequested()

  abstract class ResponseMessage
  case class ReportProcessed(exists: Boolean, id: String) extends ResponseMessage
  case class PayrollReportCompleted(report: Seq[EmployeePayPeriod]) extends ResponseMessage
  case class PayrollReportUnavailable() extends ResponseMessage
  case class ReportParseFailed() extends ResponseMessage
  case class DatabaseFailure(th: Throwable) extends ResponseMessage
}

object GlobalMessages {
  abstract class GlobalMessage
  case class ReportReady(report: TimeReport) extends GlobalMessage
}
