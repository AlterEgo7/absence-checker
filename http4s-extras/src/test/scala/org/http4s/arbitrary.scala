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

package org.http4s

import org.http4s.headers.W3CTraceParent
import org.scalacheck.{Arbitrary, Gen}
import scodec.bits.ByteVector

object arbitrary {

  def byteVectorOfSize(size: Int): Gen[ByteVector] =
    Gen.infiniteLazyList(Gen.hexChar).map(_.take(size).mkString).map(ByteVector.fromValidHex(_))

  val w3CTraceParentGenerator: Gen[W3CTraceParent] =
    for {
      traceId  <- byteVectorOfSize(16)
      parentId <- byteVectorOfSize(8)
      sampled  <- Gen.oneOf(true, false)
    } yield W3CTraceParent(W3CTraceParent.Version.Version_00, traceId, parentId, sampled)

  implicit val arbitraryW3CTraceParent: Arbitrary[W3CTraceParent] =
    Arbitrary(w3CTraceParentGenerator)
}
