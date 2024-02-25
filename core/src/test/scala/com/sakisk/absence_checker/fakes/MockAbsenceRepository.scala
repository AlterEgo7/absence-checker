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

package com.sakisk.absence_checker.fakes

import cats.Functor
import cats.effect.Ref
import cats.syntax.all.*
import com.sakisk.absence_checker.repositories.AbsenceRepository
import com.sakisk.absence_checker.types.*
import fs2.*

class MockAbsenceRepository[F[_]: Functor](state: Ref[F, Map[AbsenceId, Absence]]) extends AbsenceRepository[F]:

  val getState: F[Map[AbsenceId, Absence]] = state.get

  override def upsert(absence: Absence): F[Unit] =
    state.update(_.updated(absence.id, absence))

  override def find(absenceId: AbsenceId): F[Option[Absence]] =
    state.get.map(state => state.get(absenceId))

  override def streamAll: fs2.Stream[F, Absence] =
    Stream.eval(state.get).flatMap(map => Stream.iterable(map.values))

  override def streamWithEndAfter(endThreshold: AbsenceEndTime): fs2.Stream[F, Absence] =
    Stream.eval(state.get).flatMap(map =>
      Stream.iterable(map.filter { case (_, absence) => absence.end.value.isAfter(endThreshold.value) }.values)
    )

  override def isDateRangeEmpty(start: AbsenceStartTime, end: AbsenceEndTime): F[Boolean] =
    state.get.map:
      _.forall:
        case (_, absence) => start.value.isAfter(end.value) || absence.start.value.isAfter(end.value)

  override def delete(absenceId: AbsenceId): F[Unit] = state.update(_.removed(absenceId))
