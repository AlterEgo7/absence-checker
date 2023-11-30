import sbt.*

object Dependencies {
  private val http4sVersion      = "0.23.23"
  private val fs2Version         = "3.9.3"
  private val weaverVersion      = "0.8.3"
  private val smithy4sVersion    = "0.18.3"
  private val smithyModelVersion = "1.41.1"
  private val alloyVersion       = "0.2.8"
  private val logbackVersion     = "1.4.12"
  private val cirisVersion       = "3.5.0"
  private val ironVersion        = "2.3.0"

  val Fs2          = "co.fs2"        %% "fs2-core"            % fs2Version
  val Http4sServer = "org.http4s"    %% "http4s-ember-server" % http4sVersion
  val Http4sDsl    = "org.http4s"    %% "http4s-dsl"          % http4sVersion
  val Logback      = "ch.qos.logback" % "logback-classic"     % logbackVersion

  val Smithy4sHttp4s        = "com.disneystreaming.smithy4s" %% "smithy4s-http4s"         % smithy4sVersion
  val Smithy4sHttp4sSwagger = "com.disneystreaming.smithy4s" %% "smithy4s-http4s-swagger" % smithy4sVersion

  val Weaver           = "com.disneystreaming" %% "weaver-cats"       % weaverVersion % Test
  val WeaverScalacheck = "com.disneystreaming" %% "weaver-scalacheck" % weaverVersion % Test

  val SmithyModel = "software.amazon.smithy"    % "smithy-model" % smithyModelVersion
  val Alloy       = "com.disneystreaming.alloy" % "alloy-core"   % alloyVersion

  val Ciris       = "is.cir"             %% "ciris"        % cirisVersion
  val CirisHttp4s = "is.cir"             %% "ciris-http4s" % cirisVersion
  val Iron        = "io.github.iltotore" %% "iron"         % ironVersion
  val IronCiris   = "io.github.iltotore" %% "iron-ciris"   % ironVersion
}
