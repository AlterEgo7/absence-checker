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

package com.sakisk.absence_checker.sql.codecs

import com.sakisk.absence_checker.types.*
import skunk.Codec
import skunk.codec.all.*
import smithy4s.Timestamp

val absenceId: Codec[AbsenceId] = uuid.imap(AbsenceId.apply)(_.value)

val absenceName: Codec[AbsenceName] = text.imap(AbsenceName.apply)(_.value)

val smithyTimestamp: Codec[Timestamp] = timestamptz.imap(Timestamp.fromOffsetDateTime)(_.toOffsetDateTime)

val absenceStartTime: Codec[AbsenceStartTime] = smithyTimestamp.imap(AbsenceStartTime.apply)(_.value)
val absenceEndTime: Codec[AbsenceEndTime]     = smithyTimestamp.imap(AbsenceEndTime.apply)(_.value)
