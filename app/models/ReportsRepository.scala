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

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.{ Future, ExecutionContext }
import slick.jdbc.JdbcProfile

@Singleton
class ReportsRepository @Inject() (db_cfg_provider: DatabaseConfigProvider)(implicit ctx: ExecutionContext) {
  private val _cfg = db_cfg_provider.get[JdbcProfile]

  import _cfg._
  import profile.api._

  private val _hours = TableQuery[HoursTable]

  def exists(id: String): Future[Boolean] = db.run {
    _hours.filter(_.time_sheet_id === id).exists.result
  }
}
