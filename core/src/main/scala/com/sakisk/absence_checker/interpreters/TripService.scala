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

package com.sakisk.absence_checker.interpreters

import cats.MonadThrow
import cats.data.{NonEmptyList, Validated, ValidatedNel}
import cats.syntax.all.*
import com.sakisk.absence_checker.algebras.{TripAlgebra, TripCommands, TripQueries}
import com.sakisk.absence_checker.repositories.TripRepository
import com.sakisk.absence_checker.types.{Trip, TripId}

import java.time.Duration as JavaDuration

class TripCommandHandler[F[_]: MonadThrow](repo: TripRepository[F]) extends TripCommands[F]:
  override def put(trip: Trip): F[Unit] =
    validateTrip(trip) match
      case Validated.Valid(trip)     => repo.upsert(trip)
      case Validated.Invalid(errors) => TripValidationError(errors).raiseError

  override def delete(tripId: TripId): F[Unit] =
    repo.delete(tripId)

  private def validateTrip(trip: Trip): ValidatedNel[String, Trip] =
    val validateEndIsAfterStart: Validated[String, Unit] =
      Validated.cond(
        trip.end.value.isAfter(trip.start.value),
        (),
        s"Trip [$trip] start datetime is after end datetime"
      )

    val validateDurationIsLessThanAYear: Validated[String, Unit] =
      Validated.cond(
        JavaDuration.between(trip.start.value.toOffsetDateTime, trip.end.value.toOffsetDateTime).abs.toDays <= 365,
        (),
        s"Trip [$trip] has a duration longer than a year"
      )

    (
      validateEndIsAfterStart.toValidatedNel,
      validateDurationIsLessThanAYear.toValidatedNel
    ).mapN { case (_, _) => trip }

end TripCommandHandler

class TripQueryExecutor[F[_]](repo: TripRepository[F]) extends TripQueries[F]:

  override def getTrip(tripId: TripId): F[Option[Trip]] =
    repo.find(tripId)

  override def listTrips: fs2.Stream[F, Trip] =
    repo.streamAll

end TripQueryExecutor

class TripService[F[_]: MonadThrow](repo: TripRepository[F]) extends TripAlgebra[F]:
  private val queryExecutor  = TripQueryExecutor[F](repo)
  private val commandHandler = TripCommandHandler[F](repo)

  override def getTrip(tripId: TripId): F[Option[Trip]] = queryExecutor.getTrip(tripId)
  override def listTrips: fs2.Stream[F, Trip]           = queryExecutor.listTrips
  override def put(trip: Trip): F[Unit]                 = commandHandler.put(trip)
  override def delete(tripId: TripId): F[Unit]          = commandHandler.delete(tripId)
end TripService

sealed trait TripServiceError extends Throwable

final case class TripValidationError(errors: NonEmptyList[String]) extends TripServiceError:
  override def getMessage = s"Trip validation failed:${errors.mkString_("\n\t", "\n\t", "\n\n")}"
