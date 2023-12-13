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

package com.sakisk.absence_checker.http

import com.sakisk.absence_checker
import smithy4s.http4s.swagger.docs
import cats.effect.*
import com.sakisk.absence_checker.repositories.AbsenceRepository
import org.http4s.HttpRoutes
import smithy4s.http4s.SimpleRestJsonBuilder

class Routes[F[_]: Async](repo: AbsenceRepository[F]):
  val routes: Resource[F, HttpRoutes[F]] = SimpleRestJsonBuilder.routes(AbsenceCheckerImpl[F](repo)).resource

  val docRoutes: HttpRoutes[F] = docs[F](absence_checker.AbsenceCheckerService)

object Routes:
  def apply[F[_]: Async](repo: AbsenceRepository[F]): Routes[F] = new Routes(repo)
