package com.sakisk.absense_checker.generators

import com.sakisk.absense_checker.types.*
import org.scalacheck.Gen
import smithy4s.Timestamp

import java.time.Instant

val tripGenerator: Gen[Trip] =
  for {
    id              <- Gen.uuid.map(TripId.apply)
    start           <- Gen.chooseNum(-2208988800000L, 1701899033399L)
    durationSeconds <- Gen.chooseNum(86400, 86400 * 30)
    startInstant     = Instant.ofEpochMilli(start)
    startDateTime    = TripStartTime(Timestamp.fromInstant(startInstant))
    endInstant       = startInstant.plusSeconds(durationSeconds)
    endDateTime      = TripEndTime(Timestamp.fromInstant(endInstant))
    name            <- Gen.alphaStr.map(TripName.apply)

  } yield Trip(id, startDateTime, endDateTime, name)
