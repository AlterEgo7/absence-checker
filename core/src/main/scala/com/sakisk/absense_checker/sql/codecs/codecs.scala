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

package com.sakisk.absense_checker.sql.codecs

import com.sakisk.absense_checker.types.*
import skunk.Codec
import skunk.codec.all.*
import smithy4s.Timestamp

val tripId: Codec[TripId] = uuid.imap(TripId.apply)(_.value)

val tripName: Codec[TripName] = text.imap(TripName.apply)(_.value)

val smithyTimestamp: Codec[Timestamp] = timestamptz.imap(Timestamp.fromOffsetDateTime)(_.toOffsetDateTime)

val tripStartTime: Codec[TripStartTime] = smithyTimestamp.imap(TripStartTime.apply)(_.value)
val tripEndTime: Codec[TripEndTime]     = smithyTimestamp.imap(TripEndTime.apply)(_.value)
