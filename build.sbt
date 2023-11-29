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

lazy val root = tlCrossRootProject.aggregate(core)

lazy val core = project
  .in(file("core"))
  .settings(
    name        := "core",
    description := "Core data types and operations",
    libraryDependencies ++= Seq(Weaver, WeaverScalacheck)
  )
