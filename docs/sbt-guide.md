# SBT Guide for Scala Forge

## What is sbt?
`sbt` (Simple Build Tool) is the standard build tool for Scala projects. It handles:
1. Compilation
2. Dependency management
3. Testing
4. Packaging
5. Running applications

## Why Scala Forge uses sbt
1. Best support for Scala-first projects.
2. Great multi-module support.
3. Strong plugin ecosystem (formatting, assembly, Docker packaging, etc.).
4. Fast developer workflow with incremental compile.

## Key Files
1. `build.sbt`: main project build definition.
2. `project/build.properties`: sbt launcher version.
3. `project/plugins.sbt`: build plugins.

## Common Commands
Run from repository root:

```powershell
sbt compile
```

```powershell
sbt test
```

```powershell
sbt run
```

Pass CLI args to Scala Forge:

```powershell
sbt "run new spark-app spark-sentinel --dry-run"
```

```powershell
sbt "run doctor ./spark-sentinel"
```

```powershell
sbt "run upgrade ./spark-sentinel --spark 3.5.3 --dry-run"
```

## Useful Interactive Workflow
Start interactive shell:

```powershell
sbt
```

Then run tasks without restarting sbt:

```text
compile
test
run --help
```

This is usually faster than launching `sbt` per command.

## Multi-module Basics
In multi-module projects you can scope tasks:

```text
project cli
compile
test
```

Or from command line:

```powershell
sbt "project cli" compile
```

## Dependency and Build Hygiene
1. Pin important versions (Scala, Spark, Iceberg).
2. Keep compiler flags strict in CI.
3. Add tests for generated templates.
4. Avoid unnecessary plugin sprawl.

## Running sbt with Docker
If sbt is not installed locally, use Docker:

```powershell
docker compose build scala-forge
docker compose run --rm scala-forge sbt "run --help"
```

See also: [run-with-docker.md](/C:/repository/scala-forge/docs/run-with-docker.md)

## Troubleshooting
1. `sbt: command not found`
   - Use Docker workflow or install sbt + JDK 17.
2. Slow first build
   - Expected: dependency download and cache warmup.
3. Build cache issues
   - Restart sbt shell; if needed, clear local caches carefully.

## Team Recommendation
1. Use `sbt` as the default build tool for Scala Forge.
2. Use Docker-based sbt for consistent onboarding.
3. Keep commands documented in project docs and CI logs.
