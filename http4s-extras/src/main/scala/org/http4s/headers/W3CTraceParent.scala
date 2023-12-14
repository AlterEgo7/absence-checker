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

import cats.parse.*
import org.http4s.{Header, ParseResult}
import org.http4s.headers.W3CTraceParent.Version.Version_00
import org.http4s.util.{Renderable, Writer}
import scodec.bits.{BitVector, ByteVector}
import org.typelevel.ci.*

object W3CTraceParent {
  sealed trait Version {
    def value: Byte
  }
  object Version       {
    case object Version_00 extends Version {
      override val value = 0.toByte
    }
  }

  def parse(s: String): ParseResult[W3CTraceParent] =
    ParseResult.fromParser(parser, "Invalid traceparent header")(s)

  val parser: Parser[W3CTraceParent] = {
    import Accumulator.*
    ((Parser.repExactlyAs[Char, String](Rfc5234.digit, 2).flatMap {
      case "00" => Parser.pure(Version_00)
      case "ff" => Parser.failWith("traceparent version 0xFF is invalid")
      case _    => Parser.failWith("traceparent parser accepts only version 00")
    } <* Parser.char('-')) ~
    (Parser.repExactlyAs[Char, String](Rfc5234.hexdig, 32) <* Parser.char('-')) ~
    (Parser.repExactlyAs[Char, String](Rfc5234.hexdig, 16) <* Parser.char('-')) ~
    Parser.repExactlyAs[Char, String](Rfc5234.hexdig, 2))
      .map { case (((version, traceIdHex), parentIdHex), traceFlagChars) =>
        val traceId    = ByteVector.fromValidHex(traceIdHex)
        val parentId   = ByteVector.fromValidHex(parentIdHex)
        val traceFlags = BitVector.fromValidHex(traceFlagChars)
        val sampled    = traceFlags.last

        W3CTraceParent(version, traceId, parentId, sampled)
      }
  }

  implicit val headerInstance: Header[W3CTraceParent, Header.Single] =
    Header.createRendered(
      ci"traceparent",
      h =>
        new Renderable {
          override def render(writer: Writer) = w3CTraceParentRenderValueImpl(writer, h)
        },
      parse
    )

  private def w3CTraceParentRenderValueImpl(writer: Writer, w3CTraceParent: W3CTraceParent): writer.type = {
    w3CTraceParent.versionFormat match
      case Version.Version_00 => writer.append("00")

    writer.append('-')
    writer.append(w3CTraceParent.traceId.toHex)
    writer.append('-')
    writer.append(w3CTraceParent.parentId.toHex)
    writer.append('-')
    val sampledByte = if (w3CTraceParent.sampled) 1.toByte else 0.toByte
    writer.append(BitVector(sampledByte).toHex)
  }
}

final case class W3CTraceParent(
  versionFormat: W3CTraceParent.Version,
  traceId: ByteVector,
  parentId: ByteVector,
  sampled: Boolean
)
