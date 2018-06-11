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
