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

package com.sakisk.absense_checker.interpreters

import cats.effect.{IO, Ref}
import com.sakisk.absense_checker.fakes.MockTripRepository
import com.sakisk.absense_checker.types.*
import weaver.SimpleIOSuite
import com.sakisk.absense_checker.generators.*
import org.scalacheck.Gen
import smithy4s.Timestamp
import weaver.scalacheck.*
import fs2.*

import java.time.temporal.ChronoUnit
import java.util.UUID
import scala.util.Random

object TripServiceTests extends SimpleIOSuite with Checkers:

  test("creates a valid trip"):
    forall(tripGenerator): trip =>
      for {
        state    <- Ref.of(Map.empty[TripId, Trip])
        repo      = MockTripRepository[IO](state)
        service   = TripCommandHandler(repo)
        _        <- service.put(trip)
        newState <- repo.getState
      } yield expect(
        newState.toList == List(trip.id -> trip)
      )

  test("fails with a validation error when the trip has a duration of more than a year"):
    (for {
      state    <- Ref.of(Map.empty[TripId, Trip])
      repo      = MockTripRepository[IO](state)
      service   = TripCommandHandler(repo)
      generated = tripGenerator.generateSingle
      trip      = generated.copy(end =
                    TripEndTime(Timestamp.fromInstant(generated.start.value.toInstant.plus(366, ChronoUnit.DAYS)))
                  )
      _        <- service.put(trip)
    } yield failure("Should fail with validation failure")).handleError(t =>
      expect.all(t.isInstanceOf[TripValidationError], t.getMessage.contains("has a duration longer than a year"))
    )

  test("fails with a validation error when the trip end time is before the start time"):
    (for {
      state    <- Ref.of(Map.empty[TripId, Trip])
      repo      = MockTripRepository[IO](state)
      service   = TripCommandHandler(repo)
      generated = tripGenerator.generateSingle
      trip      = generated.copy(end =
                    TripEndTime(Timestamp.fromInstant(generated.start.value.toInstant.minus(1, ChronoUnit.DAYS)))
                  )
      _        <- service.put(trip)
    } yield failure("Should fail with validation failure")).handleError(t =>
      expect.all(t.isInstanceOf[TripValidationError], t.getMessage.contains("start datetime is after end datetime"))
    )

  test("getTrip returns an existing trip"):
    forall(tripGenerator): trip =>
      for {
        state   <- Ref.of(Map(trip.id -> trip))
        repo     = MockTripRepository[IO](state)
        service  = TripQueryExecutor(repo)
        fetched <- service.getTrip(trip.id)
      } yield expect(fetched.contains(trip))

  test("getTrip return None for non-existent trip"):
    for {
      state   <- Ref.of(Map.empty[TripId, Trip])
      service  = TripQueryExecutor(MockTripRepository(state))
      fetched <- service.getTrip(TripId(UUID.randomUUID()))
    } yield expect(fetched.isEmpty)

  test("listTrips returns all trips"):
    forall(Gen.listOfN(10, tripGenerator)): trips =>
      Stream.eval(Ref.of(Map.from(trips.map(t => t.id -> t)))).flatMap: state =>
        val service = TripQueryExecutor(MockTripRepository(state))
        service.listTrips
      .compile.toList
        .map(fetchedTrips => expect(fetchedTrips.toSet == trips.toSet))

  test("deletes an existing trip"):
    forall(Gen.listOfN(10, tripGenerator)): trips =>
      for {
        state   <- Ref.of(Map.from(trips.map(t => t.id -> t)))
        service  = TripService(MockTripRepository(state))
        randomId = Random.shuffle(trips).head.id
        _       <- service.delete(randomId)
        fetched <- service.getTrip(randomId)
      } yield expect(fetched.isEmpty)
