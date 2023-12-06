package com.sakisk.absense_checker.repositories

import cats.effect.*
import com.sakisk.absense_checker.TestResources
import skunk.Session
import weaver.IOSuite
import org.typelevel.otel4s.trace.Tracer.Implicits.noop
import com.sakisk.absense_checker.generators.*
import org.scalacheck.*
import org.scalacheck.rng.Seed

object TripRepositoryPostgresTests extends IOSuite with TestResources:
  override type Res = Resource[IO, Session[IO]]

  override def sharedResource = sessionPool[IO]

  test("upsert with new id inserts new trip"): postgres =>
    val trip = tripGenerator.pureApply(Gen.Parameters.default, Seed.random())
    val repo = TripRepositoryPostgres(postgres)

    for {
      noTrip   <- repo.find(trip.id)
      _        <- repo.upsert(trip)
      inserted <- repo.find(trip.id)
    } yield expect.all(
      noTrip.isEmpty,
      inserted.contains(trip)
    )

end TripRepositoryPostgresTests
