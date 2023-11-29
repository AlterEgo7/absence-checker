import sbt.*

object Dependencies {
  private val http4sVersion = "1.0.0-M40"
  private val fs2Version    = "3.9.3"
  private val weaverVersion = "0.8.3"

  val Fs2          = "co.fs2"     %% "fs2-core"            % fs2Version
  val Http4sServer = "org.http4s" %% "http4s-ember-server" % http4sVersion
  val Http4sDsl    = "org.http4s" %% "http4s-dsl"          % http4sVersion

  val Weaver = "com.disneystreaming" %% "weaver-cats" % weaverVersion % Test
  val WeaverScalacheck = "com.disneystreaming" %% "weaver-scalacheck" % weaverVersion % Test
}
