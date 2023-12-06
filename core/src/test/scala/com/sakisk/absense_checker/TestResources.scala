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
import com.dimafeng.testcontainers.JdbcDatabaseContainer.CommonParams
import com.dimafeng.testcontainers.PostgreSQLContainer
import fs2.io.net.Network
import org.testcontainers.utility.DockerImageName
import org.typelevel.otel4s.trace.Tracer
import skunk.*

import scala.concurrent.duration.*
import sys.process.*

trait TestResources:

  def sessionPool[F[_]: Async: Tracer: Network: Console]: SessionPool[F] =
    val postgresContainerDef = PostgreSQLContainer.Def(
      dockerImageName = DockerImageName.parse("postgres:16-alpine"),
      databaseName = "absense_checker",
      username = "absense_checker",
      password = "pass",
      mountPostgresDataToTmpfs = true,
      urlParams = Map("search_path" -> "public", "sslmode" -> "disable"),
      commonJdbcParams = CommonParams(
        startupTimeout = 10.seconds,
        connectTimeout = 2.seconds,
        initScriptPath = None
      )
    )

    for {
      container <- Resource.fromAutoCloseable {
                     val c = postgresContainerDef.createContainer().container
                     Sync[F].blocking(c.start()).as(c)
                   }

      atlasUrl     =
        s"postgres://${container.getUsername}:${container.getPassword}@${container.getHost}:${container.getMappedPort(5432)}/${container.getDatabaseName}?search_path=public&sslmode=disable"
      command      =
        s"atlas schema apply --auto-approve --url $atlasUrl --to file://${sys.env("ATLAS_SCHEMA_FILE")}"
      -           <- Resource.eval(Sync[F].blocking("pwd".!))
      _           <- Resource.eval(Sync[F].blocking(command.!))
      sessionPool <- Session.pooled[F](
                       host = container.getHost,
                       port = container.getMappedPort(5432),
                       user = container.getUsername,
                       database = container.getDatabaseName,
                       password = container.getPassword.some,
                       max = 5
                     )
    } yield sessionPool

end TestResources
