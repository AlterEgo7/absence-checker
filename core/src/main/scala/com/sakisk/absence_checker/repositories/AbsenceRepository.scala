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
import fs2.*
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.trace.Tracer
import skunk.*
import skunk.syntax.all.*

import java.time.format.DateTimeFormatter

trait AbsenceRepository[F[_]] {
  def upsert(absence: Absence): F[Unit]

  def find(absenceId: AbsenceId): F[Option[Absence]]

  def streamAll: Stream[F, Absence]

  def streamWithEndAfter(endThreshold: AbsenceEndTime): Stream[F, Absence]

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

      override def streamAll: Stream[F, Absence] =
        Stream.resource(Tracer[F].span("Select all Absences").resource).flatMap: res =>
          Stream.resource(postgres).translate(res.trace).flatMap: session =>
            session.stream(selectAllSql)(Void, 1024)
              .translate(res.trace)

      override def streamWithEndAfter(endThreshold: AbsenceEndTime): Stream[F, Absence] =
        Stream.resource(Tracer[F].span(
          "Select all Absences after",
          Attribute[String](
            "Absence end time",
            endThreshold.value.toOffsetDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
          )
        ).resource).flatMap: _ =>
          Stream.resource(postgres).flatMap: session =>
            session.stream(selectWithEndAfter)(endThreshold, 1024)

      override def delete(absenceId: AbsenceId): F[Unit] =
        Tracer[F].span("Delete Absence", Attribute("id", absenceId.value.toString)).surround:
          postgres.use(_.execute(deleteSql)(absenceId)).void

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

  end AbsenceRepositorySql

end AbsenceRepositoryPostgres
