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

package com.sakisk.absense_checker

import cats.effect.*
import cats.effect.std.Console
import cats.syntax.all.*
import fs2.io.net.Network
import org.typelevel.otel4s.trace.Tracer
import skunk.Session

sealed abstract case class AppResources[F[_]](
  tracedDbPool: Tracer[F] => Resource[F, Session[F]]
)

object AppResources:

  private def mkDbPool[F[_]: Temporal: Network: Console](config: DatabaseConfig): Resource[
    F,
    Tracer[F] => Resource[F, Session[F]]
  ] =
    Session.pooledF[F](
      host = config.host.toString,
      port = config.port.value,
      user = config.username.value,
      database = config.database.value,
      password = config.password.value.value.some,
      max = 5
    )

  def make[F[_]: Temporal: Network: Console](config: Config): Resource[F, AppResources[F]] =
    for {
      dbPool <- mkDbPool(config.databaseConfig)
    } yield new AppResources(dbPool) {}

end AppResources
