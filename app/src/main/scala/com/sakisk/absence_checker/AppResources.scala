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

import cats.effect.*
import cats.effect.std.Console
import cats.syntax.all.*
import com.sakisk.absence_checker.repositories.{AbsenceRepository, AbsenceRepositoryPostgres}
import fs2.io.net.Network
import org.typelevel.log4cats.Logger
import org.typelevel.otel4s.trace.Tracer
import skunk.*
import skunk.implicits.*
import skunk.codec.all.*

sealed abstract case class AppResources[F[_]](
  tracedDbPool: Resource[F, Session[F]],
  absenceRepository: AbsenceRepository[F]
)

object AppResources:

  private def mkDbPool[F[_]: Logger: Tracer: Temporal: Network: Console](config: DatabaseConfig): Resource[
    F,
    Resource[F, Session[F]]
  ] = {
    def checkPostgresConnection(postgres: Resource[F, Session[F]]) = postgres.use: session =>
      session.unique(sql"SELECT VERSION();".query(text))
        .flatMap: v =>
          Logger[F].info(s"Connected to Postgres: $v")

    Session.pooled[F](
      host = config.host.toString,
      port = config.port.value,
      user = config.username.value,
      database = config.database.value,
      password = config.password.value.value.some,
      max = 5
    )
      .evalTap(checkPostgresConnection)
  }

  def make[F[_]: Logger: Tracer: Temporal: Network: Console](config: Config): Resource[F, AppResources[F]] =
    for {
      dbPool           <- mkDbPool(config.databaseConfig)
      absenceRepository = AbsenceRepositoryPostgres(dbPool)
    } yield new AppResources(dbPool, absenceRepository) {}

end AppResources
