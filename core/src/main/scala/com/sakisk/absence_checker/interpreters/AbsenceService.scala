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
import com.sakisk.absence_checker.algebras.{AbsenceAlgebra, AbsenceCommands, AbsenceQueries}
import com.sakisk.absence_checker.repositories.AbsenceRepository
import com.sakisk.absence_checker.types.*

import java.time.Duration as JavaDuration

class AbsenceCommandHandler[F[_]: MonadThrow](repo: AbsenceRepository[F]) extends AbsenceCommands[F]:
  override def put(absence: Absence): F[Unit] =
    validateAbsence(absence) match
      case Validated.Valid(absence)  =>
        repo.isDateRangeEmpty(absence.start, absence.end).ifM(
          repo.upsert(absence),
          AbsenceValidationError(NonEmptyList.one("Another absence is persisted in the same date range.")).raiseError
        )
      case Validated.Invalid(errors) => AbsenceValidationError(errors).raiseError

  override def delete(absenceId: AbsenceId): F[Unit] =
    repo.delete(absenceId)

  private def validateAbsence(absence: Absence): ValidatedNel[String, Absence] =
    val validateEndIsAfterStart: Validated[String, Unit] =
      Validated.cond(
        absence.end.value.isAfter(absence.start.value),
        (),
        s"Absence [$absence] start datetime is after end datetime"
      )

    val validateDurationIsLessThanAYear: Validated[String, Unit] =
      Validated.cond(
        JavaDuration.between(absence.start.value.toOffsetDateTime, absence.end.value.toOffsetDateTime).abs.toDays <= 365,
        (),
        s"Absence [$absence] has a duration longer than a year"
      )

    (
      validateEndIsAfterStart.toValidatedNel,
      validateDurationIsLessThanAYear.toValidatedNel
    ).mapN { case (_, _) => absence }

end AbsenceCommandHandler

class AbsenceQueryExecutor[F[_]](repo: AbsenceRepository[F]) extends AbsenceQueries[F]:

  override def getAbsence(absenceId: AbsenceId): F[Option[Absence]] =
    repo.find(absenceId)

  override def listAbsences: F[List[Absence]] =
    repo.listAll

end AbsenceQueryExecutor

class AbsenceService[F[_]: MonadThrow](repo: AbsenceRepository[F]) extends AbsenceAlgebra[F]:
  private val queryExecutor  = AbsenceQueryExecutor[F](repo)
  private val commandHandler = AbsenceCommandHandler[F](repo)

  override def getAbsence(absenceId: AbsenceId): F[Option[Absence]] = queryExecutor.getAbsence(absenceId)
  override def listAbsences: F[List[Absence]]                       = queryExecutor.listAbsences
  override def put(absence: Absence): F[Unit]                       = commandHandler.put(absence)
  override def delete(absenceId: AbsenceId): F[Unit]                = commandHandler.delete(absenceId)
end AbsenceService

sealed trait AbsenceServiceError extends Throwable

final case class AbsenceValidationError(errors: NonEmptyList[String]) extends AbsenceServiceError:
  override def getMessage = s"Absence validation failed:${errors.mkString_("\n\t", "\n\t", "\n\n")}"
