package com.sakisk.absense_checker.repositories

import cats.effect.MonadCancelThrow
import cats.effect.kernel.Resource
import cats.syntax.all.*
import com.sakisk.absense_checker.sql.codecs.*
import com.sakisk.absense_checker.types.{Trip, TripEndTime, TripId}
import fs2.*
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.trace.Tracer
import skunk.*
import skunk.syntax.all.*

import java.time.format.DateTimeFormatter

trait TripRepository[F[_]] {
  def upsert(trip: Trip): F[Unit]

  def find(tripId: TripId): F[Option[Trip]]

  def streamAll: Stream[F, Trip]

  def streamWithEndAfter(endThreshold: TripEndTime): Stream[F, Trip]
}

object TripRepositoryPostgres:
  import TripRepositorySql.*

  def apply[F[_]: Tracer: MonadCancelThrow](postgres: Resource[F, Session[F]]): TripRepository[F] =
    new TripRepository[F]:
      override def upsert(trip: Trip): F[Unit] =
        Tracer[F].span("Upsert Trip").surround:
          postgres.use(_.execute(upsertSql)(trip)).void

      override def find(tripId: TripId): F[Option[Trip]] =
        Tracer[F].span("Find Trip").surround:
          postgres.use(_.option(findSql)(tripId))

      override def streamAll: Stream[F, Trip] =
        Stream.resource(Tracer[F].span("Select all Trips").resource).flatMap: _ =>
          Stream.resource(postgres).flatMap: session =>
            session.stream(selectAllSql)(Void, 1024)

      override def streamWithEndAfter(endThreshold: TripEndTime): Stream[F, Trip] =
        Stream.resource(Tracer[F].span(
          "Select all Trips after",
          Attribute[String](
            "Trip end time",
            endThreshold.value.toOffsetDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
          )
        ).resource).flatMap: _ =>
          Stream.resource(postgres).flatMap: session =>
            session.stream(selectWithEndAfter)(endThreshold, 1024)

  private object TripRepositorySql:
    val codec: Codec[Trip] = (tripId *: tripStartTime *: tripEndTime *: tripName).to[Trip]

    val findSql: Query[TripId, Trip] =
      sql"""
           SELECT * FROM trips
           WHERE id = $tripId
         """.query(codec).to[Trip]

    val upsertSql: Command[Trip] =
      sql"""
           INSERT INTO trips as t(id, start, "end", name)
           VALUES ($codec)
           ON CONFLICT (id)
           DO UPDATE SET name = t.name, start = t.start, "end" = t."end"
         """.command

    val selectAllSql: Query[Void, Trip] =
      sql"""
           SELECT * FROM trips
         """.query(codec).to[Trip]

    val selectWithEndAfter: Query[TripEndTime, Trip] =
      sql"""
           SELECT * FROM trips
           WHERE "end" > $tripEndTime
         """.query(codec).to[Trip]

  end TripRepositorySql

end TripRepositoryPostgres
