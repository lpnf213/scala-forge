package com.scalaforge.cli

import java.nio.file.{Files, Path}
import scala.jdk.CollectionConverters._

object DoctorValidator {
  sealed trait Severity { def label: String }
  case object Pass extends Severity { val label = "PASS" }
  case object Warn extends Severity { val label = "WARN" }
  case object Fail extends Severity { val label = "FAIL" }

  final case class Finding(severity: Severity, message: String)
  final case class DoctorReport(projectPath: Path, findings: List[Finding]) {
    def hasFailures: Boolean = findings.exists(_.severity == Fail)
  }

  def validate(projectPath: Path): DoctorReport = {
    val buildSbt = projectPath.resolve("build.sbt")
    val buildProps = projectPath.resolve("project").resolve("build.properties")
    val srcMain = projectPath.resolve("src").resolve("main")
    val srcTest = projectPath.resolve("src").resolve("test")

    val required = List(
      checkExists(buildSbt, "build.sbt exists"),
      checkExists(buildProps, "project/build.properties exists"),
      checkExists(srcMain, "src/main exists"),
      checkExists(srcTest, "src/test exists")
    )

    val buildChecks =
      if (Files.exists(buildSbt)) validateBuildSbt(buildSbt)
      else List(Finding(Warn, "Skipping build.sbt content checks because build.sbt is missing"))

    DoctorReport(projectPath.toAbsolutePath.normalize(), required ++ buildChecks)
  }

  private def checkExists(path: Path, successMessage: String): Finding =
    if (Files.exists(path)) Finding(Pass, successMessage)
    else Finding(Fail, s"Missing required path: ${path.toAbsolutePath.normalize()}")

  private def validateBuildSbt(buildSbt: Path): List[Finding] = {
    val content = Files.readAllLines(buildSbt).asScala.mkString("\n")
    List(
      checkContains(content, "scalaVersion", "build.sbt defines scalaVersion"),
      checkContains(content, "mainClass", "build.sbt defines mainClass"),
      checkContains(content, "scalatest", "build.sbt includes ScalaTest dependency")
    )
  }

  private def checkContains(content: String, token: String, successMessage: String): Finding =
    if (content.contains(token)) Finding(Pass, successMessage)
    else Finding(Warn, s"Recommended setting not found in build.sbt: '$token'")
}
