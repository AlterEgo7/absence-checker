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

package com.sakisk.absence_checker

import cats.syntax.all.*
import ciris.*
import ciris.http4s.*
import com.comcast.ip4s.*
import com.sakisk.absence_checker.config.*
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.iltotore.iron.ciris.given
import io.github.iltotore.iron.cats.given

final case class Config(httpServerConfig: HttpServerConfig, databaseConfig: DatabaseConfig)
final case class HttpServerConfig(host: Host, port: Port)

final case class DatabaseConfig(
  host: Host,
  port: Port,
  username: DatabaseUsername,
  password: Secret[DatabasePassword],
  database: DatabaseName
)

def httpServerConfig[F[_]]: ConfigValue[F, HttpServerConfig] =
  (
    env("SERVER_HOST").as[Host].default(host"0.0.0.0"),
    env("SERVER_PORT").as[Port].default(port"9000")
  )
    .parMapN(HttpServerConfig.apply)

def databaseConfig[F[_]]: ConfigValue[F, DatabaseConfig] =
  (
    env("DB_HOST").as[Host].default(host"localhost"),
    env("DB_PORT").as[Port].default(port"5432"),
    env("DB_USERNAME").as[DatabaseUsername],
    env("DB_PASSWORD").as[DatabasePassword].secret,
    env("DB_NAME").as[DatabaseName]
  )
    .parMapN(DatabaseConfig.apply)

def appConfig[F[_]]: ConfigValue[F, Config] =
  (
    httpServerConfig[F],
    databaseConfig[F]
  )
    .parMapN(Config.apply)
