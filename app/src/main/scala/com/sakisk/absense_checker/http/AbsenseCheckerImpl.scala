package com.sakisk.absense_checker.http

import cats.Applicative
import cats.syntax.all.*
import com.sakisk.absense_checker.*
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
