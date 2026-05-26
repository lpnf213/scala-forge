package com.scalaforge.cli

object ScalaForgeCli {

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
        sys.exit(1)
    }
  }

  private def run(command: Command): Int =
    command match {
      case NewCommand(template, projectName, options, flags) =>
        runNew(template, projectName, options, flags)

      case DoctorCommand(projectPath) =>
        println(s"[doctor] target=$projectPath")
        println("Project validation is not implemented yet.")
        0

      case UpgradeCommand(projectPath, options, flags) =>
        println(s"[upgrade] target=$projectPath")
        if (options.nonEmpty) println(s"[upgrade] options=${formatOptions(options)}")
        if (flags.nonEmpty) println(s"[upgrade] flags=${flags.toList.sorted.mkString(",")}")
        println("Upgrade assistant is not implemented yet.")
        0

      case HelpCommand =>
        printUsage()
        0
    }

  private def runNew(
      template: String,
      projectName: String,
      options: Map[String, String],
      flags: Set[String]
  ): Int = {
    val dryRun = flagEnabled("dry-run", options, flags)
    val overwrite = flagEnabled("overwrite", options, flags)
    val scalaVersion = options.getOrElse("scala", "2.13.14")

    template.toLowerCase match {
      case "cli-app" =>
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
            0
          case Left(error) =>
            Console.err.println(s"Error: $error")
            1
        }

      case other =>
        Console.err.println(
          s"Error: template '$other' is not implemented yet. Currently supported: cli-app"
        )
        1
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
