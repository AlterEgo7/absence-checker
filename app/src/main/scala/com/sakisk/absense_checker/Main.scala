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
import cats.syntax.all.*
import com.sakisk.absense_checker.http.Routes
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.syntax.all.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.otel4s.java.OtelJava
import org.typelevel.otel4s.trace.Tracer

object Main extends ResourceApp.Forever:
  override def run(args: List[String]): Resource[IO, Unit] =
    for {
      tracerBuilder    <- Resource.eval(OtelJava.global[IO].map(_.tracerProvider.tracer("Absense Checker")))
      config           <- Resource.eval(config[IO].load)
      given Tracer[IO] <- Resource.eval(tracerBuilder.get)
      given Logger[IO] <- Resource.eval(Slf4jLogger.create[IO])
      appResources     <- AppResources.make[IO](config)
      dbPool           <- appResources.tracedDbPool
      routes           <- Routes[IO].routes
      _                <- EmberServerBuilder.default[IO]
                            .withPort(config.httpServerConfig.port)
                            .withHost(config.httpServerConfig.host)
                            .withHttpApp((routes <+> Routes[IO].docRoutes).orNotFound)
                            .build
    } yield ()
