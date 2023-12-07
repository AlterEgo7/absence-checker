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

package com.sakisk.absense_checker.repositories

import cats.effect.*
import cats.syntax.all.*
import com.sakisk.absense_checker.TestResources
import com.sakisk.absense_checker.generators.*
import com.sakisk.absense_checker.types.*
import org.typelevel.otel4s.trace.Tracer.Implicits.noop
import skunk.*
import smithy4s.Timestamp
import weaver.IOSuite

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

object TripRepositoryPostgresTests extends IOSuite with TestResources:
  override type Res = Resource[IO, Session[IO]]

  override def sharedResource = sessionPool[IO]

  test("upsert with new id inserts new trip"): postgres =>
    val trip = tripGenerator.generateSingle
    val repo = TripRepositoryPostgres(postgres)

    for {
      noTrip   <- repo.find(trip.id)
      _        <- repo.upsert(trip)
      inserted <- repo.find(trip.id)
    } yield expect.all(
      noTrip.isEmpty,
      inserted.contains(trip)
    )

  test("upsert with existing id updates the trip"): postgres =>
    val trip = tripGenerator.generateSingle
    val repo = TripRepositoryPostgres(postgres)

    for {
      _           <- repo.upsert(trip)
      firstInsert <- repo.find(trip.id)
      _           <- repo.upsert(trip.copy(name = TripName("new_name")))
      updated     <- repo.find(trip.id)
    } yield expect.all(
      firstInsert.isDefined,
      firstInsert.get.name == trip.name,
      updated.isDefined,
      updated.get.name == TripName("new_name")
    )

  test("find finds existing trip and returns None for non-existent"): postgres =>
    val trip = tripGenerator.generateSingle
    val repo = TripRepositoryPostgres(postgres)

    for {
      _           <- repo.upsert(trip)
      nonExistent <- repo.find(TripId(UUID.randomUUID()))
      existing    <- repo.find(trip.id)
    } yield expect.all(
      nonExistent.isEmpty,
      existing.isDefined,
      existing.get == trip
    )

  test("streamWithEndAfter returns all trips"): postgres =>
    val trips = Vector.fill(5)(tripGenerator.generateSingle)
    val repo  = TripRepositoryPostgres(postgres)
    for {
      _   <- trips.traverse(repo.upsert)
      all <- repo.streamAll.compile.toList
    } yield expect(trips.toSet.subsetOf(all.toSet))

  test("streamWithEndAfter correctly applies the filter"): postgres =>
    val trip1 = Trip(
      id = TripId(UUID.randomUUID()),
      name = TripName("trip1"),
      start = TripStartTime(Timestamp.fromInstant(Instant.now.minus(7, ChronoUnit.DAYS))),
      end = TripEndTime(Timestamp.fromInstant(Instant.now.minus(5, ChronoUnit.DAYS)))
    )

    val trip2 = Trip(
      id = TripId(UUID.randomUUID()),
      name = TripName("trip2"),
      start = TripStartTime(Timestamp.fromInstant(Instant.now.minus(4, ChronoUnit.DAYS))),
      end = TripEndTime(Timestamp.fromInstant(Instant.now.minus(3, ChronoUnit.DAYS)))
    )

    val repo = TripRepositoryPostgres(postgres)

    for {
      _     <- List(trip1, trip2).traverse(repo.upsert)
      after <- repo.streamWithEndAfter(
                 TripEndTime(Timestamp.fromInstant(Instant.now.minus(4, ChronoUnit.DAYS)))
               ).compile.toList
    } yield expect(after.map(_.id) == List(trip2.id))

end TripRepositoryPostgresTests
