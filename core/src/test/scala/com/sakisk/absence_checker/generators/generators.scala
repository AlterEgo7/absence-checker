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

package com.sakisk.absence_checker.generators

import com.sakisk.absence_checker.types.*
import org.scalacheck.Gen
import org.scalacheck.rng.Seed
import smithy4s.Timestamp

import java.time.Instant

val absenceGenerator: Gen[Absence] =
  for {
    id              <- Gen.uuid.map(AbsenceId.apply)
    start           <- Gen.chooseNum(-2208988800000L, 1701899033399L)
    durationSeconds <- Gen.chooseNum(86400, 86400 * 30)
    startInstant     = Instant.ofEpochMilli(start)
    startDateTime    = AbsenceStartTime(Timestamp.fromInstant(startInstant))
    endInstant       = startInstant.plusSeconds(durationSeconds)
    endDateTime      = AbsenceEndTime(Timestamp.fromInstant(endInstant))
    name            <- Gen.alphaStr.map(AbsenceName.apply)

  } yield Absence(id, startDateTime, endDateTime, name)

extension [A](gen: Gen[A])
  def generateSingle: A = gen.pureApply(Gen.Parameters.default, Seed.random())
