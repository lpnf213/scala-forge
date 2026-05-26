ThisBuild / organization := "com.scalaforge"
ThisBuild / scalaVersion := "2.13.14"
ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala-forge",
    Compile / unmanagedSourceDirectories += baseDirectory.value / "modules" / "cli" / "src" / "main" / "scala",
    Compile / mainClass := Some("com.scalaforge.cli.ScalaForgeCli"),
    assembly / mainClass := Some("com.scalaforge.cli.ScalaForgeCli"),
    assembly / assemblyJarName := "scala-forge.jar",
    assembly / assemblyMergeStrategy := {
      case "module-info.class" => sbtassembly.MergeStrategy.discard
      case x =>
        val old = (assembly / assemblyMergeStrategy).value
        old(x)
    },
    scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-encoding", "utf8")
  )
