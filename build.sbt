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

ThisBuild / githubWorkflowPublishTargetBranches := Seq()
ThisBuild / githubWorkflowOSes                  := Seq("ubuntu-latest")
ThisBuild / githubWorkflowJavaVersions          := Seq(JavaSpec.graalvm("17"))

ThisBuild / tlFatalWarnings   := true
ThisBuild / tlCiScalafmtCheck := true

ThisBuild / Test / fork := true

lazy val root = project.in(file(".")).aggregate(core, app)

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
    name        := "app",
    description := "Application classes",
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
    run / javaOptions ++= Seq(
      "-Dotel.java.global-autoconfigure.enabled=true",
      "-Dotel.service.name=jaeger-example",
      "-Dotel.metrics.exporter=none"
    )
  )
  .enablePlugins(Smithy4sCodegenPlugin)
  .enablePlugins(AtlasPlugin)
  .dependsOn(core)
