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

package com.sakisk.absense_checker.fakes

import cats.Functor
import cats.effect.Ref
import cats.syntax.all.*
import com.sakisk.absense_checker.repositories.TripRepository
import com.sakisk.absense_checker.types.{Trip, TripEndTime, TripId}
import fs2.*

class MockTripRepository[F[_]: Functor](state: Ref[F, Map[TripId, Trip]]) extends TripRepository[F]:

  val getState: F[Map[TripId, Trip]] = state.get

  override def upsert(trip: Trip): F[Unit] =
    state.update(_.updated(trip.id, trip))

  override def find(tripId: TripId): F[Option[Trip]] =
    state.get.map(state => state.get(tripId))

  override def streamAll: fs2.Stream[F, Trip] =
    Stream.eval(state.get).flatMap(map => Stream.iterable(map.values))

  override def streamWithEndAfter(endThreshold: TripEndTime): fs2.Stream[F, Trip] =
    Stream.eval(state.get).flatMap(map =>
      Stream.iterable(map.filter { case (_, trip) => trip.end.value.isAfter(endThreshold.value) }.values)
    )

  override def delete(tripId: TripId): F[Unit] = state.update(_.removed(tripId))
