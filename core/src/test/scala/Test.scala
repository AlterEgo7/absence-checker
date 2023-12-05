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

import cats.effect.*
import com.sakisk.absense_checker.TestResources
import org.typelevel.otel4s.trace.Tracer.Implicits.noop
import skunk.*
import skunk.implicits.*
import skunk.codec.all.*
import weaver.IOSuite

object Test extends IOSuite with TestResources:

  override type Res = Resource[IO, Session[IO]]

  override def sharedResource = sessionPool[IO]

  pureTest("a simple pure test"):
    expect(List(1).length == 1)

  test("should get a postgres session"): postgres =>
    postgres.use: session =>
      session.unique(sql"SELECT 1".query(int4))
        .map(i => expect(i == 1))

end Test
