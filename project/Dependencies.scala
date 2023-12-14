import sbt.*

object Dependencies {
  private val http4sVersion              = "0.23.24"
  private val fs2Version                 = "3.9.3"
  private val weaverVersion              = "0.8.3"
  private val smithy4sVersion            = "0.18.3"
  private val smithyModelVersion         = "1.42.0"
  private val alloyVersion               = "0.2.8"
  private val logbackVersion             = "1.4.14"
  private val cirisVersion               = "3.5.0"
  private val ironVersion                = "2.3.0"
  private val skunkVersion               = "1.1.0-M2"
  private val otel4sVersion              = "0.3.0"
  private val log4catsVersion            = "2.6.0"
  private val openTelemetryVersion       = "1.33.0"
  private val testContainersScalaVersion = "0.41.0"

  val Fs2          = "co.fs2"        %% "fs2-core"            % fs2Version
  val Http4sCore   = "org.http4s"    %% "http4s-core"         % http4sVersion
  val Http4sServer = "org.http4s"    %% "http4s-ember-server" % http4sVersion
  val Http4sDsl    = "org.http4s"    %% "http4s-dsl"          % http4sVersion
  val Logback      = "ch.qos.logback" % "logback-classic"     % logbackVersion

  val Smithy4sCore          = "com.disneystreaming.smithy4s" %% "smithy4s-core"           % smithy4sVersion
  val Smithy4sHttp4s        = "com.disneystreaming.smithy4s" %% "smithy4s-http4s"         % smithy4sVersion
  val Smithy4sHttp4sSwagger = "com.disneystreaming.smithy4s" %% "smithy4s-http4s-swagger" % smithy4sVersion
  val Smithy4sCats          = "com.disneystreaming.smithy4s" %% "smithy4s-cats"           % smithy4sVersion
  val Smithy4sProtocol      = "com.disneystreaming.smithy4s"  % "smithy4s-protocol"       % smithy4sVersion

  val Weaver                      = "com.disneystreaming" %% "weaver-cats"          % weaverVersion              % Test
  val WeaverScalacheck            = "com.disneystreaming" %% "weaver-scalacheck"    % weaverVersion              % Test
  val TestContainersScala         = "com.dimafeng"        %% "testcontainers-scala" % testContainersScalaVersion % Test
  val TestContainersScalaPostgres =
    "com.dimafeng" %% "testcontainers-scala-postgresql" % testContainersScalaVersion % Test

  val SmithyModel = "software.amazon.smithy"    % "smithy-model" % smithyModelVersion
  val Alloy       = "com.disneystreaming.alloy" % "alloy-core"   % alloyVersion

  val Ciris       = "is.cir"             %% "ciris"        % cirisVersion
  val CirisHttp4s = "is.cir"             %% "ciris-http4s" % cirisVersion
  val Iron        = "io.github.iltotore" %% "iron"         % ironVersion
  val IronCiris   = "io.github.iltotore" %% "iron-ciris"   % ironVersion

  val Skunk                      = "org.tpolecat"    %% "skunk-core"                  % skunkVersion
  val Otel4s                     = "org.typelevel"   %% "otel4s-java"                 % otel4sVersion
  val OpenTelemetryExporter      = "io.opentelemetry" % "opentelemetry-exporter-otlp" % openTelemetryVersion % Runtime
  val OpenTelemetryAutoconfigure =
    "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % openTelemetryVersion % Runtime

  val Log4Cats = "org.typelevel" %% "log4cats-slf4j" % log4catsVersion
}
