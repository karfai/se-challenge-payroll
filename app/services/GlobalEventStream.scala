package services

import akka.actor.{ ActorRef, ActorSystem }
import javax.inject._

import actors._

@Singleton
class GlobalEventStream @Inject() (
  @Named("actors-payroll-report") actor_payroll_report: ActorRef,
  @Named("actors-time-report-store") actor_time_report_store: ActorRef,
  system: ActorSystem
) {
  system.eventStream.subscribe(actor_payroll_report, classOf[GlobalMessages.GlobalMessage])
  system.eventStream.subscribe(actor_time_report_store, classOf[GlobalMessages.GlobalMessage])

  def publish(m: GlobalMessages.GlobalMessage) {
    system.eventStream.publish(m)
  }
}
