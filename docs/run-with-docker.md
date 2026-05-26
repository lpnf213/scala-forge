# Run Scala Forge With Docker

## Prerequisites
1. Docker Desktop installed and running.
2. Project opened at `C:\repository\scala-forge`.

## 1) Show CLI Help
```powershell
docker compose build scala-forge
docker compose run --rm scala-forge --help
```

## 2) Run `new`
```powershell
docker compose run --rm scala-forge new spark-app spark-sentinel --scala 2.12.18 --spark 3.5.1 --dry-run
```

## 3) Run `doctor`
```powershell
docker compose run --rm scala-forge doctor ./spark-sentinel
```

## 4) Run `upgrade`
```powershell
docker compose run --rm scala-forge upgrade ./spark-sentinel --spark 3.5.3 --dry-run
```

## Notes
1. The JAR is created during `docker compose build` via `sbt assembly`.
2. The runtime container executes `java -jar /opt/scala-forge/scala-forge.jar`.
3. Your repository is mounted at `/work`, so generated files are saved on your host machine.
