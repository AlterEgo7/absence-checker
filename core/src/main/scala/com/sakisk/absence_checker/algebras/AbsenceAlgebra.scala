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

package com.sakisk.absence_checker.algebras

import com.sakisk.absence_checker.types.*

trait AbsenceAlgebra[F[_]] extends AbsenceCommands[F] with AbsenceQueries[F]

trait AbsenceCommands[F[_]] {
  def put(absence: Absence): F[Unit]

  def delete(absenceId: AbsenceId): F[Unit]
}

trait AbsenceQueries[F[_]] {
  def getAbsence(absenceId: AbsenceId): F[Option[Absence]]

  def listAbsences: F[List[Absence]]
}
