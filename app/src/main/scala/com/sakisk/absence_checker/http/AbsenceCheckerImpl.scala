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

import cats.Applicative
import cats.effect.kernel.Async
import cats.syntax.all.*
import com.sakisk.absence_checker.*
import com.sakisk.absence_checker.repositories.TripRepository
import com.sakisk.absence_checker.types.*
import smithy4s.Timestamp

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class AbsenceCheckerImpl[F[_]: Applicative: Async](repo: TripRepository[F]) extends AbsenceCheckerService[F]:

  override def insertTrip(trip: Trip): F[Unit] =
    repo.upsert(trip)

  override def listTrips(): F[ListTripsOutput] =
    repo.streamAll.compile.toList.map(trips => ListTripsOutput(trips.toSet))

  override def getTrip(id: TripId): F[Trip] =
    Trip(
      TripId(UUID.randomUUID()),
      TripStartTime(Timestamp.fromInstant(Instant.now.minus(5, ChronoUnit.DAYS))),
      TripEndTime(Timestamp.nowUTC()),
      TripName("test")
    ).pure[F]

end AbsenceCheckerImpl
