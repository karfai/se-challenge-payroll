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
