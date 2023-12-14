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

package org.http4s.headers

import cats.Show
import org.http4s.Header
import org.http4s.headers.W3CTraceParent.*
import org.typelevel.ci.*
import scodec.bits.ByteVector
import weaver.*
import weaver.scalacheck.*

object W3CTraceParentSuite extends SimpleIOSuite with Checkers {
  given Show[W3CTraceParent] = Show.fromToString

  pureTest("fails when version is not 00"):
    val input = "01-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01"

    expect(W3CTraceParent.parse(input).isLeft)

  pureTest("parses a valid representation"):
    val input       = "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01"
    val parseResult = W3CTraceParent.parse(input)

    expect(parseResult.isRight)

    val w3CTraceParent = parseResult.toOption.get

    expect.all(
      w3CTraceParent.sampled,
      w3CTraceParent.traceId.toHex == "4bf92f3577b34da6a3ce929d0e0e4736",
      w3CTraceParent.parentId.toHex == "00f067aa0ba902b7"
    )

  pureTest("correctly encodes the value"):
    val header = W3CTraceParent(
      W3CTraceParent.Version.Version_00,
      ByteVector.fromValidHex("4bf92f3577b34da6a3ce929d0e0e4736"),
      ByteVector.fromValidHex("00f067aa0ba902b7"),
      true
    )

    expect.all(
      Header[W3CTraceParent].name == ci"traceparent",
      Header[W3CTraceParent].value(header) == "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01"
    )
}
