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

import cats.effect.{IO, Ref}
import com.sakisk.absence_checker.fakes.MockAbsenceRepository
import com.sakisk.absence_checker.types.*
import weaver.SimpleIOSuite
import com.sakisk.absence_checker.generators.*
import org.scalacheck.Gen
import smithy4s.Timestamp
import weaver.scalacheck.*
import fs2.*

import java.time.temporal.ChronoUnit
import java.util.UUID
import scala.util.Random

object AbsenceServiceTests extends SimpleIOSuite with Checkers:

  test("creates a valid absence"):
    forall(absenceGenerator): absence =>
      for {
        state    <- Ref.of(Map.empty[AbsenceId, Absence])
        repo      = MockAbsenceRepository[IO](state)
        service   = AbsenceCommandHandler(repo)
        _        <- service.put(absence)
        newState <- repo.getState
      } yield expect(
        newState.toList == List(absence.id -> absence)
      )

  test("fails with a validation error when the absence has a duration of more than a year"):
    (for {
      state    <- Ref.of(Map.empty[AbsenceId, Absence])
      repo      = MockAbsenceRepository[IO](state)
      service   = AbsenceCommandHandler(repo)
      generated = absenceGenerator.generateSingle
      absence   = generated.copy(end =
                    AbsenceEndTime(Timestamp.fromInstant(generated.start.value.toInstant.plus(366, ChronoUnit.DAYS)))
                  )
      _        <- service.put(absence)
    } yield failure("Should fail with validation failure")).handleError(t =>
      expect.all(t.isInstanceOf[AbsenceValidationError], t.getMessage.contains("has a duration longer than a year"))
    )

  test("fails with a validation error when the absence end time is before the start time"):
    (for {
      state    <- Ref.of(Map.empty[AbsenceId, Absence])
      repo      = MockAbsenceRepository[IO](state)
      service   = AbsenceCommandHandler(repo)
      generated = absenceGenerator.generateSingle
      absence   = generated.copy(end =
                    AbsenceEndTime(Timestamp.fromInstant(generated.start.value.toInstant.minus(1, ChronoUnit.DAYS)))
                  )
      _        <- service.put(absence)
    } yield failure("Should fail with validation failure")).handleError(t =>
      expect.all(t.isInstanceOf[AbsenceValidationError], t.getMessage.contains("start datetime is after end datetime"))
    )

  test("getAbsence returns an existing absence"):
    forall(absenceGenerator): absence =>
      for {
        state   <- Ref.of(Map(absence.id -> absence))
        repo     = MockAbsenceRepository[IO](state)
        service  = AbsenceQueryExecutor(repo)
        fetched <- service.getAbsence(absence.id)
      } yield expect(fetched.contains(absence))

  test("getAbsence return None for non-existent absence"):
    for {
      state   <- Ref.of(Map.empty[AbsenceId, Absence])
      service  = AbsenceQueryExecutor(MockAbsenceRepository(state))
      fetched <- service.getAbsence(AbsenceId(UUID.randomUUID()))
    } yield expect(fetched.isEmpty)

  test("listAbsences returns all absences"):
    forall(Gen.listOfN(10, absenceGenerator)): absences =>
      Stream.eval(Ref.of(Map.from(absences.map(a => a.id -> a)))).flatMap: state =>
        val service = AbsenceQueryExecutor(MockAbsenceRepository(state))
        service.listAbsences
      .compile.toList
        .map(fetchedAbsences => expect(fetchedAbsences.toSet == absences.toSet))

  test("deletes an existing absence"):
    forall(Gen.listOfN(10, absenceGenerator)): absences =>
      for {
        state   <- Ref.of(Map.from(absences.map(a => a.id -> a)))
        service  = AbsenceService(MockAbsenceRepository(state))
        randomId = Random.shuffle(absences).head.id
        _       <- service.delete(randomId)
        fetched <- service.getAbsence(randomId)
      } yield expect(fetched.isEmpty)
