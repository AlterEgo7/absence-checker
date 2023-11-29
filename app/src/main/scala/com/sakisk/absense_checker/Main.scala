package com.sakisk.absense_checker

import cats.effect.{ExitCode, IO, IOApp}
import com.comcast.ip4s.*
import com.sakisk.absense_checker.http.Routes
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.syntax.all.*
import cats.syntax.all.*

object Main extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    Routes[IO].routes
      .flatMap { routes =>
        EmberServerBuilder
          .default[IO]
          .withPort(port"9000")
          .withHost(host"0.0.0.0")
          .withHttpApp((routes <+> Routes[IO].docRoutes).orNotFound)
          .build
      }
      .useForever
      .as(ExitCode.Success)
