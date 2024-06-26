/*
 * Copyright 2023 Sakis Karagiannis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sakisk.absence_checker.config

import ciris.Secret
import com.comcast.ip4s.{Host, Port}
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

import scala.annotation.targetName

opaque type DatabaseUsername = String :| Not[Empty]

extension (username: DatabaseUsername)
  @targetName("database_username_value") def value: String = username
object DatabaseUsername extends RefinedTypeOps[String, Not[Empty], DatabaseUsername]

opaque type DatabasePassword = String :| Pure
extension (password: DatabasePassword)
  @targetName("database_password_value") def value: String = password
object DatabasePassword extends RefinedTypeOps[String, Pure, DatabasePassword]

opaque type DatabaseName = String :| Pure
extension (dbName: DatabaseName)
  @targetName("database_name_value") def value: String     = dbName
object DatabaseName extends RefinedTypeOps[String, Pure, DatabaseName]

final case class DatabaseConfig(
  host: Host,
  port: Port,
  username: DatabaseUsername,
  password: Secret[DatabasePassword],
  database: DatabaseName
)
