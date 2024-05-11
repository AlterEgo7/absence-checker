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

import cats.effect.MonadCancelThrow
import cats.effect.kernel.Resource
import cats.syntax.all.*
import com.sakisk.absence_checker.sql.codecs.*
import com.sakisk.absence_checker.types.*
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.trace.Tracer
import skunk.*
import skunk.syntax.all.*

import java.time.format.DateTimeFormatter

trait AbsenceRepository[F[_]] {
  def upsert(absence: Absence): F[Unit]

  def find(absenceId: AbsenceId): F[Option[Absence]]

  def isDateRangeEmpty(start: AbsenceStartTime, end: AbsenceEndTime): F[Boolean]

  def listAll: F[List[Absence]]

  def listWithEndAfter(endThreshold: AbsenceEndTime): F[List[Absence]]

  def delete(absenceId: AbsenceId): F[Unit]
}

object AbsenceRepositoryPostgres:
  import AbsenceRepositorySql.*

  def apply[F[_]: Tracer: MonadCancelThrow](postgres: Resource[F, Session[F]]): AbsenceRepository[F] =
    new AbsenceRepository[F]:
      override def upsert(absence: Absence): F[Unit] =
        Tracer[F].span("Upsert Absence").surround:
          postgres.use(_.execute(upsertSql)(absence)).void

      override def find(absenceId: AbsenceId): F[Option[Absence]] =
        Tracer[F].span("Find Absence").surround:
          postgres.use(_.option(findSql)(absenceId))

      override def listAll: F[List[Absence]] =
        Tracer[F].span("Select all Absences").surround:
          postgres.use(_.execute(selectAllSql))

      override def listWithEndAfter(endThreshold: AbsenceEndTime): F[List[Absence]] =
        Tracer[F].span(
          "Select all Absences after",
          Attribute[String](
            "Absence end time",
            endThreshold.value.toOffsetDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
          )
        ).surround:
          postgres.use(_.execute(selectWithEndAfter)(endThreshold))

      override def delete(absenceId: AbsenceId): F[Unit] =
        Tracer[F].span("Delete Absence", Attribute("id", absenceId.value.toString)).surround:
          postgres.use(_.execute(deleteSql)(absenceId)).void

      override def isDateRangeEmpty(start: AbsenceStartTime, end: AbsenceEndTime): F[Boolean] =
        Tracer[F].span(
          "Is Date Range Empty",
          Attribute[String](
            "Absence start time",
            start.value.toOffsetDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
          ),
          Attribute[String]("Absence end time", end.value.toOffsetDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
        ).surround(postgres.use(_.option(selectInDateRange)((start, end)).map(_.isEmpty)))
    end new

  private object AbsenceRepositorySql:
    val codec: Codec[Absence] = (absenceId *: absenceStartTime *: absenceEndTime *: absenceName).to[Absence]

    val findSql: Query[AbsenceId, Absence] =
      sql"""
           SELECT * FROM absences
           WHERE id = $absenceId
         """.query(codec).to[Absence]

    val upsertSql: Command[Absence] =
      sql"""
           INSERT INTO absences
           VALUES ($codec)
           ON CONFLICT (id)
           DO UPDATE SET name = excluded.name, start = excluded.start, "end" = excluded."end"
         """.command

    val selectAllSql: Query[Void, Absence] =
      sql"""
           SELECT * FROM absences
         """.query(codec).to[Absence]

    val selectWithEndAfter: Query[AbsenceEndTime, Absence] =
      sql"""
           SELECT * FROM absences
           WHERE "end" >= $absenceEndTime
         """.query(codec).to[Absence]

    val deleteSql: Command[AbsenceId] =
      sql"""
           DELETE FROM absences
           WHERE id = $absenceId
         """.command

    val selectInDateRange: Query[(AbsenceStartTime, AbsenceEndTime), Absence] =
      sql"""
            SELECT * FROM absences
            WHERE $absenceStartTime <= "end"
            AND $absenceEndTime >= start
         """.query(codec).to[Absence]

  end AbsenceRepositorySql

end AbsenceRepositoryPostgres
