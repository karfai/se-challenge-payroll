package actors

import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

class ActorsModule extends AbstractModule with AkkaGuiceSupport {
  def configure = {
    bindActor[ParseReportActor]("actors-parse-report")
    bindActor[PayrollReportActor]("actors-payroll-report")
    bindActor[TimeReportStoreActor]("actors-time-report-store")
  }
}
