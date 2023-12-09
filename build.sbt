import Dependencies.*

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / tlBaseVersion    := "0.1"
ThisBuild / organization     := "com.sakisk"
ThisBuild / organizationName := "Sakis Karagiannis"
ThisBuild / startYear        := Some(2023)
ThisBuild / licenses         := Seq(License.Apache2)

val Scala3 = "3.3.1"

ThisBuild / scalaVersion := Scala3

ThisBuild / testFrameworks += new TestFramework("weaver.framework.CatsEffect")

ThisBuild / tlFatalWarnings   := true
ThisBuild / tlCiScalafmtCheck := true

ThisBuild / Test / fork := true

lazy val root = project.in(file(".")).aggregate(core, app)
  .settings(
    jibDockerBuild / aggregate := false,
    jibImageBuild / aggregate  := false
  )

lazy val core = project
  .in(file("core"))
  .settings(
    name        := "core",
    description := "Core data types and operations",
    libraryDependencies ++= Seq(
      Logback,
      Smithy4sCore,
      SmithyModel,
      Alloy,
      Log4Cats,
      Skunk,
      Weaver,
      WeaverScalacheck,
      TestContainersScala,
      TestContainersScalaPostgres
    )
  )
  .enablePlugins(Smithy4sCodegenPlugin)

lazy val app = project
  .in(file("app"))
  .settings(
    name                   := "app",
    description            := "Application classes",
    libraryDependencies ++= Seq(
      Smithy4sHttp4s,
      Smithy4sHttp4sSwagger,
      Http4sServer,
      Logback,
      SmithyModel,
      Alloy,
      CirisHttp4s,
      IronCiris,
      Skunk,
      Otel4s,
      Log4Cats,
      OpenTelemetryExporter,
      OpenTelemetryAutoconfigure
    ),
    run / fork             := true,
    run / javaOptions ++= Seq(
      "-Dotel.java.global-autoconfigure.enabled=true",
      "-Dotel.service.name=jaeger-example",
      "-Dotel.metrics.exporter=none"
    ),
    jibBaseImage           := "eclipse-temurin:21-jre-alpine",
    jibUseCurrentTimestamp := true,
    jibName                := "absense-checker",
    jibVersion             := "0.1",
    jibEnvironment ++= Map(
      "DB_HOST"     -> "postgres",
      "DB_PORT"     -> sys.env("DB_PORT"),
      "DB_USERNAME" -> sys.env("DB_USERNAME"),
      "DB_PASSWORD" -> sys.env("DB_PASSWORD"),
      "DB_NAME"     -> sys.env("DB_NAME"),
      "JAVA_OPTS"   -> "-Dotel.java.global-autoconfigure.enabled=true -Dotel.service.name=jaeger-example -Dotel.metrics.exporter=none"
    ),
    jibTcpPorts += 9000
  )
  .enablePlugins(Smithy4sCodegenPlugin)
  .enablePlugins(AtlasPlugin)
  .enablePlugins(JibPlugin)
  .dependsOn(core)

ThisBuild / githubWorkflowPublishTargetBranches := Seq()
ThisBuild / githubWorkflowOSes                  := Seq("ubuntu-latest")
ThisBuild / githubWorkflowJavaVersions          := Seq(JavaSpec.graalvm("17"))
ThisBuild / githubWorkflowUseSbtThinClient      := true
ThisBuild / githubWorkflowEnv ++= Map(
  "ATLAS_SCHEMA_FILE" -> "${{ github.workspace }}/db/local/schema.hcl",
  "DB_USERNAME"       -> "absense_checker",
  "DB_PASSWORD"       -> "pass",
  "DB_NAME"           -> "absense_checker",
  "DB_PORT"           -> "5432"
)
ThisBuild / githubWorkflowBuildPreamble         := Seq(
  WorkflowStep.Run(
    commands = List("curl -sSf https://atlasgo.sh | sh -s -- --community -y"),
    name = "Install atlas cli".some
  ),
  WorkflowStep.Run(commands =
    List(
      "mkdir -p ~/image-cache"
    )
  ),
  WorkflowStep.Use(
    name = "Cache docker images".some,
    id = "image-cache".some,
    ref =
      UseRef.Public(
        owner = "actions",
        repo = "cache",
        ref = "v1"
      ),
    params = Map(
      "path" -> "~/image-cache",
      "key"  -> "image-cache-${{ runner.os }}"
    )
  ),
  WorkflowStep.Run(
    cond = "steps.image-cache.outputs.cache-hit != 'true'".some,
    commands = List(
      "docker pull postgres:16-alpine",
      "docker save -o ~/image-cache/postgres.tar postgres:16-alpine"
    )
  ),
  WorkflowStep.Run(
    cond = "steps.image-cache.outputs.cache-hit == 'true'".some,
    commands = List(
      "docker load -i ~/image-cache/postgres.tar"
    )
  )
)
