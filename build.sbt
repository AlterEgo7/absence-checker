import Dependencies.*

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / tlBaseVersion    := "0.1"
ThisBuild / organization     := "com.sakisk"
ThisBuild / organizationName := "SakisK"
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

lazy val root = project.in(file(".")).aggregate(core, app)

lazy val core = project
  .in(file("core"))
  .settings(
    name        := "core",
    description := "Core data types and operations",
    libraryDependencies ++= Seq(Weaver, WeaverScalacheck)
  )

lazy val app = project
  .in(file("app"))
  .settings(
    name        := "app",
    description := "Application classes",
    libraryDependencies ++= Seq(Smithy4sHttp4s, Smithy4sHttp4sSwagger, Http4sServer, Logback, SmithyModel, Alloy)
  )
  .enablePlugins(Smithy4sCodegenPlugin)
  .dependsOn(core)
