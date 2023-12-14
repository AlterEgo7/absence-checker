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

package org.http4s.middleware

import cats.data.{Kleisli, OptionT}
import cats.effect.MonadCancelThrow
import cats.syntax.all.*
import org.http4s.Status.ClientError
import org.http4s.headers.W3CTraceParent
import org.http4s.syntax.all.*
import org.http4s.{Header, Http, HttpRoutes, Response, Status as ResponseStatus}
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.trace.{Span, SpanKind, Status, Tracer}

object ServerTracing:
  def apply[F[_]: MonadCancelThrow: Tracer, G[_]](httpApp: Http[F, G]): Http[F, G] =
    Kleisli: req =>
      val traceParentCarrier: Map[String, String] =
        req.headers.get(Header[W3CTraceParent].name).fold(Map.empty)(values =>
          Map(Header[W3CTraceParent].name.toString -> values.head.value)
        )
      Tracer[F].joinOrRoot(traceParentCarrier):
        Tracer[F].spanBuilder(s"${req.method.name} ${req.uri.path}")
          .addAttribute(Attribute("http.method", req.method.name))
          .addAttribute(Attribute("http.url", req.uri.renderString))
          .withSpanKind(SpanKind.Server)
          .build
          .use: span =>
            for {
              response        <- httpApp(req)
              _               <- span.addAttribute(Attribute("http.status_code", response.status.code.toLong))
              _               <- setSpanStatus(span, response)
              traceParentValue = W3CTraceParent(
                                   W3CTraceParent.Version.Version_00,
                                   span.context.traceId,
                                   span.context.spanId,
                                   span.context.traceFlags.isSampled
                                 )
            } yield response.putHeaders(traceParentValue.toRaw1)

  def httpRoutes[F[_]: MonadCancelThrow: Tracer](httpRoutes: HttpRoutes[F]): HttpRoutes[F] =
    implicit val tracer: Tracer[OptionT[F, _]] = Tracer[F].mapK[OptionT[F, _]]
    apply(httpRoutes)

  private def setSpanStatus[F[_], G[_]](span: Span[F], response: Response[G]): F[Unit] =
    response match {
      case _ if response.status.isSuccess                                                     => span.setStatus(Status.Ok)
      case _ if response.status.code == 404                                                   => span.setStatus(Status.Ok)
      case clientErrorResponse if response.status.responseClass == ResponseStatus.ClientError =>
        span.setStatus(Status.Error, clientErrorResponse.status.reason)

      case serverErrorResponse =>
        span.setStatus(Status.Error, serverErrorResponse.status.reason)
    }
