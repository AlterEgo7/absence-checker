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
