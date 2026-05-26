# Scala Forge - Development Tasks

## 1. Scope
This document translates the Scala Forge product specification into an executable engineering backlog, from MVP to advanced roadmap phases.

## 2. Delivery Milestones
1. `M0`: Project bootstrap and CI baseline.
2. `M1`: CLI Core + Template Engine + build generation.
3. `M2`: Spark/Iceberg/Lakehouse templates.
4. `M3`: Docker/Kubernetes/Helm/Observability.
5. `M4`: Doctor + Upgrade Assistant.
6. `M5`: Docs hardening, quality, release readiness.

## 3. Global Definition of Done
1. Command behavior documented with examples.
2. Unit tests for core logic and validators.
3. Integration tests for generated project smoke runs.
4. Lint, format, and CI passing.
5. Generated templates runnable locally.
6. README + docs updated.

## 4. Task Backlog by Epic

### Epic 1 - CLI Core (Status: In Progress)
Legend:
`[x]` Done
`[-]` In Progress
`[ ]` Pending

Tasks:
1. `[x]` Implement command surface: `new`, `doctor`, `upgrade`.
2. `[x]` Implement parser for global flags and subcommand options.
3. `[x]` Add strict validation and actionable error messages.
4. `[x]` Add `--dry-run` output mode with generation preview.
5. `[x]` Add `--overwrite` safety checks.
6. `[-]` Add exit codes and structured console logging.
7. `[x]` Add command help and examples.

Acceptance:
1. `[x]` `scala-forge --help` and subcommand helps are complete.
2. `[x]` Invalid inputs return non-zero with clear diagnostics.
3. `[x]` Dry-run prints intended file changes without writing.

### Epic 2 - Template Engine (Status: Not Started)
1. Define template metadata model.
2. Create renderer pipeline (template selection, variable interpolation, write plan).
3. Add file conflict detection.
4. Add deterministic file ordering and idempotency checks.
5. Add golden tests for generated outputs.

Acceptance:
1. All declared templates render deterministically.
2. Golden tests cover all template roots.

### Epic 3 - Build System Support (Status: In Progress)
1. Generate `build.sbt`, `plugins.sbt`, `build.properties`.
2. Implement Scala/Spark/Iceberg version wiring.
3. Add compiler options (strict/non-strict profiles).
4. Generate test dependencies and baseline test config.
5. Add assembly/package configuration for runnable artifacts.

Acceptance:
1. Fresh project builds with `sbt compile`.
2. Strict mode applies stronger compiler settings.

### Epic 4 - Docker (Status: In Progress)
1. Generate multi-stage `Dockerfile`.
2. Generate `.dockerignore`.
3. Generate `docker-compose.yml` for local dependencies.
4. Add non-root runtime user and healthcheck patterns.
5. Add environment variable configuration examples.

Acceptance:
1. `docker build` succeeds for generated projects.
2. `docker compose up` boots local environment.

### Epic 5 - Kubernetes (Status: Not Started)
1. Generate namespace/service account/RBAC manifests.
2. Generate deployment/service/job/cronjob manifests.
3. Generate Spark submit job manifest.
4. Generate ConfigMap and secret example manifests.
5. Add resource limit/request defaults and probes.

Acceptance:
1. Manifests pass `kubectl apply --dry-run=client`.
2. Spark job template runs with configurable image/args.

### Epic 6 - Helm (Status: Not Started)
1. Generate chart scaffold (`Chart.yaml`, `values.yaml`, templates).
2. Parameterize image, resources, env, secrets, and Spark configs.
3. Add per-environment values overlays.
4. Add chart linting tasks.

Acceptance:
1. `helm lint` passes.
2. `helm template` renders valid manifests.

### Epic 7 - Spark Templates (Status: Not Started)
1. Generate Spark application module layout.
2. Add `SparkSessionFactory` and `JobConfig`.
3. Add sample DataFrame transformations.
4. Add local Spark tests.
5. Add `spark-submit` scripts and runtime config.

Acceptance:
1. Example Spark job runs in local mode.
2. Unit and Spark-local tests pass.

### Epic 8 - Streaming Templates (Status: Not Started)
1. Add Kafka source/sink integration scaffold.
2. Add checkpointing and watermark examples.
3. Add `foreachBatch` and stateful aggregation examples.
4. Add local Kafka docker-compose profile.
5. Add failure handling and retry examples.

Acceptance:
1. Local streaming pipeline starts and processes sample events.
2. Checkpoint recovery is demonstrated in tests/docs.

### Epic 9 - Iceberg Templates (Status: Not Started)
1. Add Spark + Iceberg runtime dependencies.
2. Add catalog and warehouse local config.
3. Add examples for `CREATE`, `MERGE`, `UPDATE`, `DELETE`.
4. Add time-travel and snapshot query examples.
5. Add maintenance scripts (rewrite/expire snapshots).

Acceptance:
1. Iceberg sample job creates and mutates tables.
2. Time-travel query examples execute successfully.

### Epic 10 - Lakehouse Templates (Status: Not Started)
1. Generate medallion modules (`raw`, `bronze`, `silver`, `gold`).
2. Add ingestion and schema enforcement examples.
3. Add deduplication and CDC patterns.
4. Add schema evolution workflow.
5. Add orchestration scripts and runbook.

Acceptance:
1. End-to-end sample pipeline produces gold outputs.
2. CDC flow and dedupe behavior are validated by tests.

### Epic 11 - Observability (Status: Not Started)
1. Generate structured logging setup.
2. Add correlation ID propagation helpers.
3. Add metrics API wrappers and default counters/timers.
4. Add execution tracking hooks for jobs.
5. Add Prometheus-friendly metric naming conventions.

Acceptance:
1. Logs are structured and query-friendly.
2. Metrics endpoint or export path is documented and testable.

### Epic 12 - Testing (Status: Not Started)
1. Add baseline test template for each generated project type.
2. Add integration test harness for generated projects.
3. Add Spark local integration tests.
4. Add property-based test examples.
5. Add template golden snapshots in CI.

Acceptance:
1. CI validates generation + compile + test for all templates.
2. Regression in template output is detected automatically.

### Epic 13 - Project Doctor (Status: Not Started)
1. Build compatibility matrix for Scala/Spark/Iceberg.
2. Implement project scanner for build/dependency config.
3. Add Docker/K8s manifest checks.
4. Implement weighted health score model.
5. Add actionable remediation suggestions.

Acceptance:
1. `scala-forge doctor ./project` outputs score + findings + fixes.
2. Compatibility violations are detected correctly.

### Epic 14 - Upgrade Assistant (Status: Not Started)
1. Implement version update planner.
2. Add compatibility checks before mutation.
3. Add migration rewrite support for build files.
4. Add breaking-change warning catalogue.
5. Add `--dry-run` diff preview for upgrades.

Acceptance:
1. Upgrade command prints explicit change plan.
2. Build files update safely with rollback guidance.

### Epic 15 - Documentation (Status: In Progress)
1. Generate project README per template.
2. Generate `docs/architecture.md`.
3. Maintain `docs/class-diagram.md` as the source-of-truth object model.
4. Generate `docs/local-development.md`.
5. Generate `docs/deployment.md`.
6. Generate `docs/troubleshooting.md`.
7. Add ADR template and optional ADR bootstrap.

Acceptance:
1. Generated docs are complete enough for new team onboarding.
2. Operational runbooks include common failure scenarios.

## 5. Template-by-Template Task Matrix

### `cli-app`
1. CLI entrypoint, config, logging, tests, README template.

### `library`
1. Public API package, versioning policy, test harness, docs.

### `spark-app`
1. Batch job skeleton, sample transforms, Spark tests, submit scripts.

### `spark-streaming-app`
1. Kafka stream scaffold, checkpoint/watermark, local streaming env.

### `iceberg-app`
1. Iceberg catalog config, DML examples, maintenance scripts.

### `lakehouse-app`
1. Medallion layers, CDC/dedupe flow, quality checks, orchestration.

### `multi-module`
1. Core/shared/app module boundaries, dependency layering rules.

### `platform-service`
1. HTTP service scaffold, health endpoints, metrics/logging integration.

### `agent-tool`
1. Agent workflow scaffold, tool interfaces, config and safety patterns.

## 6. Cross-Cutting Engineering Tasks
1. Architecture Decision Records for major choices.
2. Coding standards and lint/format enforcement.
3. Security baseline (dependency scanning, secret handling).
4. Release/versioning strategy and changelog automation.
5. Example repository fixtures for integration validation.
6. Developer productivity scripts (bootstrap, check, release).

## 7. Suggested Execution Order
1. Build `M0` and `M1` first (CLI + rendering foundation).
2. Deliver `spark-app` and `iceberg-app` first among data templates.
3. Add platform outputs (Docker/K8s/Helm) once template core is stable.
4. Build Doctor and Upgrade last after enough generated structure exists.
5. Finish with docs quality pass and release hardening.

## 8. Phase Roadmap Tasks (Long-Term)

### Phase 1 - Scala Foundations (Projects 1-10)
1. Create each project as an independent module/repo starter from Scala Forge output.
2. Define learning objective, runtime architecture, and benchmark tests for each.
3. Publish per-project completion checklist and sample exercises.

### Phase 2 - Spark Engineering (11-15)
1. Build DAG/benchmark/profiling utilities with reproducible datasets.
2. Add performance harness and metric baselines.

### Phase 3 - Streaming & Distributed Systems (16-18)
1. Build chaos scenarios and scheduler simulations.
2. Add deterministic replay tests for event flows.

### Phase 4 - Lakehouse & Iceberg (19-30)
1. Build diagnostic CLIs and simulation tools around snapshots/manifests/cost.
2. Add governance validation and schema policy enforcement.

### Phase 5 - Advanced Iceberg Internals (31-38)
1. Implement manifest/spec/delete-file profiling labs.
2. Add compatibility matrix and migration assistant workflows.

### Phase 6 - Hardcore Spark Internals (39-44)
1. Implement catalyst/shuffle/memory internals labs.
2. Add event log parsers and query planning visualizers.

### Phase 7 - Query Engines & Systems Engineering (45-50)
1. Build mini engine/control-plane projects with scalability benchmarks.
2. Define production-readiness SLOs and operational game days.

## 9. Release Readiness Checklist
1. All template smoke tests green.
2. CI/CD workflows generated and validated.
3. Documentation generated and reviewed.
4. Doctor and Upgrade commands tested against at least 3 sample projects.
5. Tagged release notes with compatibility table.
