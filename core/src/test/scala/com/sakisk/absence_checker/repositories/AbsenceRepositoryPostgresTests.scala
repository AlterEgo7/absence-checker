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

package com.sakisk.absence_checker.repositories

import cats.effect.*
import cats.syntax.all.*
import com.sakisk.absence_checker.TestResources
import com.sakisk.absence_checker.generators.*
import com.sakisk.absence_checker.types.*
import org.typelevel.otel4s.trace.Tracer.Implicits.noop
import skunk.*
import smithy4s.Timestamp
import weaver.IOSuite

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

object AbsenceRepositoryPostgresTests extends IOSuite with TestResources:
  override type Res = Resource[IO, Session[IO]]

  override def sharedResource = sessionPool[IO]

  test("upsert with new id inserts new absence"): postgres =>
    val absence = absenceGenerator.generateSingle
    val repo    = AbsenceRepositoryPostgres(postgres)

    for {
      noAbsence <- repo.find(absence.id)
      _         <- repo.upsert(absence)
      inserted  <- repo.find(absence.id)
    } yield expect.all(
      noAbsence.isEmpty,
      inserted.contains(absence)
    )

  test("upsert with existing id updates the absence"): postgres =>
    val absence = absenceGenerator.generateSingle
    val repo    = AbsenceRepositoryPostgres(postgres)

    for {
      _           <- repo.upsert(absence)
      firstInsert <- repo.find(absence.id)
      _           <- repo.upsert(absence.copy(name = AbsenceName("new_name")))
      updated     <- repo.find(absence.id)
    } yield expect.all(
      firstInsert.isDefined,
      firstInsert.get.name == absence.name,
      updated.isDefined,
      updated.get.name == AbsenceName("new_name")
    )

  test("find finds existing absence and returns None for non-existent"): postgres =>
    val absence = absenceGenerator.generateSingle
    val repo    = AbsenceRepositoryPostgres(postgres)

    for {
      _           <- repo.upsert(absence)
      nonExistent <- repo.find(AbsenceId(UUID.randomUUID()))
      existing    <- repo.find(absence.id)
    } yield expect.all(
      nonExistent.isEmpty,
      existing.isDefined,
      existing.get == absence
    )

  test("streamWithEndAfter returns all absences"): postgres =>
    val absences = Vector.fill(5)(absenceGenerator.generateSingle)
    val repo     = AbsenceRepositoryPostgres(postgres)
    for {
      _   <- absences.traverse(repo.upsert)
      all <- repo.streamAll.compile.toList
    } yield expect(absences.toSet.subsetOf(all.toSet))

  test("streamWithEndAfter correctly applies the filter"): postgres =>
    val absence1 = Absence(
      id = AbsenceId(UUID.randomUUID()),
      name = AbsenceName("absence1"),
      start = AbsenceStartTime(Timestamp.fromInstant(Instant.now.minus(7, ChronoUnit.DAYS))),
      end = AbsenceEndTime(Timestamp.fromInstant(Instant.now.minus(5, ChronoUnit.DAYS)))
    )

    val absence2 = Absence(
      id = AbsenceId(UUID.randomUUID()),
      name = AbsenceName("absence2"),
      start = AbsenceStartTime(Timestamp.fromInstant(Instant.now.minus(3, ChronoUnit.DAYS))),
      end = AbsenceEndTime(Timestamp.fromInstant(Instant.now.minus(2, ChronoUnit.DAYS)))
    )

    val repo = AbsenceRepositoryPostgres(postgres)

    for {
      _        <- List(absence1, absence2).traverse(repo.upsert)
      threshold = Timestamp.fromInstant(Instant.now.minus(4, ChronoUnit.DAYS))
      after    <- repo.streamWithEndAfter(
                    AbsenceEndTime(threshold)
                  ).compile.toList
    } yield expect.all(
      after.map(_.id).contains(absence2.id),
      after.forall(_.end.value.isAfter(threshold))
    )

  test("delete correctly deletes an existing absence"): postgres =>
    val absence = absenceGenerator.generateSingle
    val repo    = AbsenceRepositoryPostgres(postgres)

    for {
      _            <- repo.upsert(absence)
      _            <- repo.delete(absence.id)
      maybeAbsence <- repo.find(absence.id)
    } yield expect(maybeAbsence.isEmpty)

end AbsenceRepositoryPostgresTests
