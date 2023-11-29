package com.sakisk.absense_checker.http

import com.sakisk.absense_checker
import smithy4s.http4s.swagger.docs
import cats.effect.*
import org.http4s.HttpRoutes
import smithy4s.http4s.SimpleRestJsonBuilder

class Routes[F[_]: Async]:
  val routes: Resource[F, HttpRoutes[F]] = SimpleRestJsonBuilder.routes(AbsenseCheckerImpl[F]).resource

  val docRoutes: HttpRoutes[F] = docs[F](absense_checker.AbsenseCheckerService)

object Routes:
  def apply[F[_]: Async]: Routes[F] = new Routes()
