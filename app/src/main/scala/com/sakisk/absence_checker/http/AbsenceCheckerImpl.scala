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

import cats.effect.MonadCancelThrow
import cats.effect.kernel.Async
import cats.syntax.all.*
import com.sakisk.absence_checker.*
import com.sakisk.absence_checker.repositories.AbsenceRepository
import com.sakisk.absence_checker.types.*

class AbsenceCheckerImpl[F[_]: Async](repo: AbsenceRepository[F]) extends AbsenceCheckerService[F]:

  override def insertAbsence(absence: Absence): F[Unit] =
    repo.upsert(absence)

  override def listAbsences(): F[ListAbsencesOutput] =
    repo.streamAll.compile.toList.map(absences => ListAbsencesOutput(absences.toSet))

  override def getAbsence(id: AbsenceId): F[Absence] =
    repo.find(id).flatMap:
      case None          => MonadCancelThrow[F].raiseError(AbsenceNotFound())
      case Some(absence) => absence.pure[F]

  override def deleteAbsence(id: AbsenceId): F[Unit] =
    repo.delete(id)

end AbsenceCheckerImpl
