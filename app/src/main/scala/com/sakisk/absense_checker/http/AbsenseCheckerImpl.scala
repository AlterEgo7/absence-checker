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

package com.sakisk.absense_checker.http

import cats.Applicative
import cats.syntax.all.*
import com.sakisk.absense_checker.*
import com.sakisk.absense_checker.types.*
import smithy4s.Timestamp

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class AbsenseCheckerImpl[F[_]: Applicative] extends AbsenseCheckerService[F]:
  override def insertTrip(id: TripId, start: TripStartTime, end: TripEndTime): F[Unit] = ???

  override def listTrips(): F[ListTripsOutput] = ???

  override def getTrip(id: TripId): F[Trip] =
    Trip(
      TripId(UUID.randomUUID()),
      TripStartTime(Timestamp.fromInstant(Instant.now.minus(5, ChronoUnit.DAYS))),
      TripEndTime(Timestamp.nowUTC())
    ).pure[F]
