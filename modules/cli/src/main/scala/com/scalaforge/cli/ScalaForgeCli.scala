package com.scalaforge.cli

import java.nio.file.{Files, Paths}

object ScalaForgeCli {
  private object ExitCode {
    val Success = 0
    val Usage = 2
    val Validation = 3
    val DoctorFailure = 4
  }

  private val SupportedTemplates = Set("cli-app")
  private val BooleanOptions = Set(
    "docker",
    "docker-compose",
    "k8s",
    "helm",
    "github-actions",
    "observability",
    "strict",
    "overwrite",
    "dry-run",
    "docs"
  )
  private val ValueOptions = Set("scala", "spark", "iceberg", "build", "test", "config", "json", "logging")
  private val AllNewOptions = BooleanOptions ++ ValueOptions
  private val CliAppUnsupportedOptions =
    Set("spark", "iceberg", "docker", "docker-compose", "k8s", "helm", "github-actions", "observability")
  private val AllowedUpgradeOptions = Set("scala", "spark", "iceberg", "dry-run")

  sealed trait Command
  final case class NewCommand(
      template: String,
      projectName: String,
      options: Map[String, String],
      flags: Set[String]
  ) extends Command
  final case class DoctorCommand(projectPath: String) extends Command
  final case class UpgradeCommand(projectPath: String, options: Map[String, String], flags: Set[String]) extends Command
  case object HelpCommand extends Command

  def main(args: Array[String]): Unit = {
    parseArgs(args.toList) match {
      case Right(command) =>
        val exitCode = run(command)
        sys.exit(exitCode)
      case Left(error) =>
        Console.err.println(s"Error: $error")
        printUsage()
        sys.exit(ExitCode.Usage)
    }
  }

  private def run(command: Command): Int =
    command match {
      case NewCommand(template, projectName, options, flags) =>
        runNew(template, projectName, options, flags)

      case DoctorCommand(projectPath) =>
        runDoctor(projectPath)

      case UpgradeCommand(projectPath, options, flags) =>
        runUpgrade(projectPath, options, flags)

      case HelpCommand =>
        printUsage()
        ExitCode.Success
    }

  private def runNew(
      template: String,
      projectName: String,
      options: Map[String, String],
      flags: Set[String]
  ): Int = {
    val templateId = template.toLowerCase
    validateNewRequest(templateId, options, flags) match {
      case Left(errors) =>
        printValidationErrors(errors)
        ExitCode.Validation
      case Right(_) =>
        val dryRun = flagEnabled("dry-run", options, flags)
        val overwrite = flagEnabled("overwrite", options, flags)
        val scalaVersion = options.getOrElse("scala", "2.13.14")

        CliAppScaffolder.scaffold(
          projectName = projectName,
          scalaVersion = scalaVersion,
          overwrite = overwrite,
          dryRun = dryRun
        ) match {
          case Right(result) =>
            val mode = if (result.dryRun) "DRY-RUN" else "APPLY"
            println(s"[$mode] Generated template 'cli-app' for project '$projectName'")
            println(s"Project root: ${result.projectRoot}")
            println("Files:")
            result.plannedFiles.foreach(path => println(s"  - $path"))
            ExitCode.Success
          case Left(error) =>
            Console.err.println(s"Error: $error")
            ExitCode.Validation
        }
    }
  }

  private def runDoctor(projectPath: String): Int = {
    val path = Paths.get(projectPath)
    if (!Files.exists(path)) {
      printValidationErrors(List(s"Project path does not exist: ${path.toAbsolutePath.normalize()}"))
      ExitCode.Validation
    } else {
      val report = DoctorValidator.validate(path)
      println(s"[doctor] target=${report.projectPath}")
      report.findings.foreach { finding =>
        println(s"[${finding.severity.label}] ${finding.message}")
      }
      if (report.hasFailures) ExitCode.DoctorFailure else ExitCode.Success
    }
  }

  private def runUpgrade(projectPath: String, options: Map[String, String], flags: Set[String]): Int = {
    val path = Paths.get(projectPath)
    val errors = validateUpgradeRequest(path, options, flags)
    if (errors.nonEmpty) {
      printValidationErrors(errors)
      ExitCode.Validation
    } else {
      println(s"[upgrade] target=$projectPath")
      if (options.nonEmpty) println(s"[upgrade] options=${formatOptions(options)}")
      if (flags.nonEmpty) println(s"[upgrade] flags=${flags.toList.sorted.mkString(",")}")
      println("Upgrade assistant is not implemented yet.")
      ExitCode.Success
    }
  }

  private def parseArgs(args: List[String]): Either[String, Command] =
    args match {
      case Nil                              => Right(HelpCommand)
      case "--help" :: _ | "-h" :: _        => Right(HelpCommand)
      case "new" :: template :: name :: xs  => parseNew(template, name, xs)
      case "doctor" :: projectPath :: Nil   => Right(DoctorCommand(projectPath))
      case "upgrade" :: projectPath :: xs   => parseUpgrade(projectPath, xs)
      case unknown :: _                     => Left(s"Unknown command: '$unknown'")
    }

  private def parseNew(template: String, projectName: String, rest: List[String]): Either[String, Command] = {
    if (template.startsWith("-")) Left("Template is required: scala-forge new <template> <project-name> [options]")
    else if (projectName.startsWith("-")) Left("Project name is required: scala-forge new <template> <project-name> [options]")
    else {
      parseOptions(rest).map { parsed =>
        NewCommand(
          template = template,
          projectName = projectName,
          options = parsed.options,
          flags = parsed.flags
        )
      }
    }
  }

  private def parseUpgrade(projectPath: String, rest: List[String]): Either[String, Command] =
    parseOptions(rest).map { parsed =>
      UpgradeCommand(projectPath = projectPath, options = parsed.options, flags = parsed.flags)
    }

  private final case class ParsedOptions(options: Map[String, String], flags: Set[String])

  private def parseOptions(tokens: List[String]): Either[String, ParsedOptions] = {
    @annotation.tailrec
    def loop(remaining: List[String], options: Map[String, String], flags: Set[String]): Either[String, ParsedOptions] =
      remaining match {
        case Nil => Right(ParsedOptions(options, flags))
        case token :: tail if token.startsWith("--") =>
          if (token.contains("=")) {
            val keyValue = token.drop(2).split("=", 2)
            if (keyValue.length != 2 || keyValue(0).trim.isEmpty || keyValue(1).trim.isEmpty) {
              Left(s"Invalid option format: '$token'")
            } else {
              loop(tail, options.updated(keyValue(0).trim, keyValue(1).trim), flags)
            }
          } else {
            tail match {
              case value :: rest if !value.startsWith("-") =>
                loop(rest, options.updated(token.drop(2), value), flags)
              case _ =>
                loop(tail, options, flags + token.drop(2))
            }
          }

        case token :: _ =>
          Left(s"Unexpected token: '$token'. Use --key value, --key=value, or --flag.")
      }

    loop(tokens, Map.empty, Set.empty)
  }

  private def formatOptions(options: Map[String, String]): String =
    options.toList.sortBy(_._1).map { case (k, v) => s"$k=$v" }.mkString(",")

  private def flagEnabled(name: String, options: Map[String, String], flags: Set[String]): Boolean =
    flags.contains(name) || options
      .get(name)
      .exists(v => Set("1", "true", "yes", "on").contains(v.trim.toLowerCase))

  private def validateNewRequest(
      template: String,
      options: Map[String, String],
      flags: Set[String]
  ): Either[List[String], Unit] = {
    val unknownOptionErrors = (options.keySet -- AllNewOptions).toList.sorted
      .map(key => s"Unknown option: --$key")
    val unknownFlagErrors = (flags -- AllNewOptions).toList.sorted
      .map(key => s"Unknown flag: --$key")
    val booleanValueErrors = options.toList.collect {
      case (key, value) if BooleanOptions.contains(key) && !isBooleanString(value) =>
        s"Option --$key must be boolean (true/false/1/0/yes/no/on/off), got '$value'"
    }
    val templateErrors =
      if (SupportedTemplates.contains(template)) Nil
      else List(s"Unsupported template '$template'. Supported templates: ${SupportedTemplates.toList.sorted.mkString(", ")}")

    val relationErrors = List(
      Option.when(flagEnabled("helm", options, flags) && !flagEnabled("k8s", options, flags))(
        "--helm requires --k8s"
      ),
      Option.when(flagEnabled("docker-compose", options, flags) && !flagEnabled("docker", options, flags))(
        "--docker-compose requires --docker"
      )
    ).flatten

    val templateSpecificErrors =
      if (template == "cli-app") {
        val disallowedKeys = (options.keySet ++ flags).intersect(CliAppUnsupportedOptions).toList.sorted
        disallowedKeys.map(key => s"Template 'cli-app' does not support --$key")
      } else Nil

    val errors = unknownOptionErrors ++ unknownFlagErrors ++ booleanValueErrors ++ templateErrors ++ relationErrors ++ templateSpecificErrors
    if (errors.nonEmpty) Left(errors) else Right(())
  }

  private def validateUpgradeRequest(
      projectPath: java.nio.file.Path,
      options: Map[String, String],
      flags: Set[String]
  ): List[String] = {
    val pathErrors =
      if (Files.exists(projectPath)) Nil
      else List(s"Project path does not exist: ${projectPath.toAbsolutePath.normalize()}")

    val allowedFlags = Set("dry-run")
    val unknownOptionErrors = (options.keySet -- AllowedUpgradeOptions).toList.sorted
      .map(key => s"Unknown option for upgrade: --$key")
    val unknownFlagErrors = (flags -- allowedFlags).toList.sorted
      .map(key => s"Unknown flag for upgrade: --$key")
    val booleanValueErrors = options.toList.collect {
      case ("dry-run", value) if !isBooleanString(value) =>
        s"Option --dry-run must be boolean (true/false/1/0/yes/no/on/off), got '$value'"
    }

    pathErrors ++ unknownOptionErrors ++ unknownFlagErrors ++ booleanValueErrors
  }

  private def isBooleanString(value: String): Boolean =
    Set("1", "0", "true", "false", "yes", "no", "on", "off").contains(value.trim.toLowerCase)

  private def printValidationErrors(errors: List[String]): Unit = {
    Console.err.println("Validation errors:")
    errors.foreach(error => Console.err.println(s"  - $error"))
  }

  private def printUsage(): Unit = {
    val usage =
      """Scala Forge
        |
        |Usage:
        |  scala-forge new <template> <project-name> [options]
        |  scala-forge doctor <project-path>
        |  scala-forge upgrade <project-path> [options]
        |
        |Examples:
        |  scala-forge new cli-app spark-sentinel --scala 2.13.14 --dry-run
        |  scala-forge doctor ./customer360
        |  scala-forge upgrade ./customer360 --spark 3.5.3 --dry-run
        |
        |Common options:
        |  --scala <version>
        |  --spark <version>
        |  --iceberg <version>
        |  --docker
        |  --docker-compose
        |  --k8s
        |  --helm
        |  --github-actions
        |  --observability
        |  --strict
        |  --overwrite
        |  --dry-run
        |""".stripMargin

    println(usage)
  }
}
