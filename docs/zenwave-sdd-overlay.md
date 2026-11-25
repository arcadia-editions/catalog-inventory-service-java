# Catalog Inventory Service (Java): ZenWave SDD overlay

What every zenwave-sdd skill and agent needs to know about this project: where the models are, its shape, its
commands, and where its test data is. `AGENTS.md` points here.

The placeholders marked `<after generation: ...>` and `<after test data: ...>` are filled in by the skill that
produces the fact (`zenwave-test-data` / `zenwave-test-scenarios` the test data row; `zenwave-gate-check` suggests the
full-suite duration).

## Where things are

Paths are relative to this repository's root. The models live in sibling repositories that must be cloned next to
this one: `../arcadia-editions-architecture` (flows) and `../catalog-inventory-api` (this context's ZDL and contracts).
The flow also references the other contexts' ZDLs: `../orders-checkout-api`, `../payments-processing-api`,
`../fulfillment-shipping-api`, `../notifications-consumer-api`.

| What | Path |
|---|---|
| Business flows (ZFL) | `../arcadia-editions-architecture/business-flows/*.zfl` (today: `place-order-flow.zfl`) |
| Domain models (ZDL), one per bounded context | `../catalog-inventory-api/domain-model.zdl` (this service); other contexts: `../<context>-api/domain-model.zdl` |
| API contracts | `../catalog-inventory-api/openapi.yml`, `../catalog-inventory-api/asyncapi.yml` (published events), `../catalog-inventory-api/asyncapi-client.yml` (consumed events), Avro schemas in `../catalog-inventory-api/avro/` |
| Glossary and ADRs | `GLOSSARY.md`, `docs/adr/` |
| Domain summary | `docs/domain.md` |
| Board of the flow in progress | `docs/tasks.md` (archived boards in `docs/tasks/`) |
| Model gap proposals | `docs/proposals/` |
| Test data and scenario matrix | `src/test/resources/data.sql` (header: scenario and coverage matrices, chains `C-01`..`C-14`, assumptions, the fixed test "now" `2026-10-14T10:00:00Z` read by tests through a fixed `Clock`, not yet in the code) and `src/test/resources/data/jpa/<Entity>/<n>.json` (the generated `TestDataLoader` reads folders by table name: fixed by board task T-01) |
| Generation script | `zenwave-scripts.zw` |

**`zenwave-scripts.zw` and `pom.xml` read the models from a released package** (`@import(...)` and `classpath:`):

- artifact and version: `com.arcadiaeditions.catalog.inventory-management.catalog-inventory:service-all:0.2.0` (the
  `@import` of `zenwave-scripts.zw` and `pom.xml` `catalog-inventory-api.version` must match); plugin dependency of
  `zenwave-sdk-maven-plugin` and `openapi-generator-maven-plugin`. The consumed event (`PaymentVoidFailed`) has no
  named Avro types, so no other context's package is needed. Repository:
  `https://maven.pkg.github.com/arcadia-editions/arcadia-editions-architecture` (server id `github` in
  `~/.m2/settings.xml`). Folder inside the jar: `com/arcadiaeditions/catalog/inventory-management/catalog-inventory/`.
- regenerate: `ZW_FILE=zenwave-scripts.zw node ../api-product-workflows/scripts/zenwave/generate-from-zdl.mjs . .`
  (`jbang zw` does not read `.zw` files; the script turns the `@import` into `-d`). Never `--force`.
- extract the models when their repository is not cloned:

  ```bash
  P=com/arcadiaeditions/catalog/inventory-management/catalog-inventory; V=$(grep -o 'service-all:[^")]*' zenwave-scripts.zw | head -1 | cut -d: -f2)
  JAR=~/.m2/repository/$P/service-all/$V/service-all-$V.jar
  mkdir -p target/models && unzip -o -q -j "$JAR" "$P/*.zdl" "$P/*.yml" -d target/models
  ```

- staleness check (prints nothing when the packaged copy matches the working copy):
  `for f in domain-model.zdl openapi.yml asyncapi.yml asyncapi-client.yml; do diff -q --strip-trailing-cr "../catalog-inventory-api/$f" "target/models/$f"; done`
- release a model change: in `catalog-inventory-api`, run `artifact-release.yml` (workflow_dispatch) for each changed
  artifact (`domain-model`, `openapi`, `asyncapi-all`; it stamps the file versions and updates the architecture
  manifest), then `service-all`; bump `catalog-inventory-api.version` and the `@import`; regenerate.
- residual coupling to `main` (not pinned by the package): the generated `asyncapi-client.yml` `$ref`s
  `payments-processing-api/main/asyncapi.yml` (channel `payment-void-failed-event-v1`) and
  `api-contract-commons/refs/heads/main` (error topics, `CommonHeaders` trait; `asyncapi.yml` too); `domain-model.zdl`
  `apis { asyncapi client PaymentsProcessingApi, zdl client PaymentsProcessingZdl }` point at `main` of
  `payments-processing-api`.

**The ZFL's `@zdl(...)` paths** (`../../<context>-api/domain-model.zdl`, relative to `business-flows/`) resolve to
the sibling clones above; no mapping needed. This service owns `catalog-inventory-api/domain-model.zdl` only.

## Shape

- Deployment: **microservices** (one bounded context per repository; Kafka and AsyncAPI with Avro payloads, Schema
  Registry; ZDL consumers use `@asyncapi`).
- Code already in place: yes (`zenwave-tasks-from-code-and-models`). Generated with ZenWave SDK: yes (2.8.1, commit
  `0d3ae27`); service methods and event listeners are still the generated `TODO`s.
- Stack: Java 25, Spring Boot 4.1.2, Spring Cloud Stream (Kafka), MapStruct, ArchUnit, Maven (no wrapper: `mvn`).
- Persistence: `jpa` (PostgreSQL; Testcontainers via docker-compose in tests).
- Organization prefix and packages: `com.arcadiaeditions` (base package `com.arcadiaeditions.catalog.inventory`),
  `CleanHexagonalProjectLayout`: `core.domain`, `core.application`, `core.inbound`, `core.outbound`,
  `adapters.web`, `infrastructure` (JPA), `config`; generated AsyncAPI SDK outside the hexagon in
  `asyncapi.provider` (`asyncapi.yml`) and `asyncapi.client` (`asyncapi-client.yml`), Avro models in `events.avro`.

## Commands

```bash
# JDK and tools: Java 25 and Maven on PATH (SDKMAN), JBang with the `zw` alias, Docker
export JAVA_HOME=~/.sdkman/candidates/java/current
# regenerate after a model change (the person runs it; never the orchestrator or its workers):
# the 3 plugin blocks of zenwave-scripts.zw, in order (BackendApplicationDefaultPlugin, OpenAPIControllersPlugin,
# SpringWebTestClientPlugin); the IDE play button shows each `jbang zw -p <Plugin> ...` command. Never --force.
# Then: mvn -DskipTests clean test-compile (runs the OpenAPI / AsyncAPI Maven generators) and review git diff.
# compile, and collect MapStruct warnings
mkdir -p target && mvn -q -DskipTests clean compile 2>&1 | tee target/compile.log
# focused tests of a task (the implementer runs only these)
mvn -q test -Dtest='<TestClass>'
# full suite with integration tests and containers (once per slice review)
# <after generation: full suite takes about N minutes, needs Docker (Testcontainers starts the docker-compose PostgreSQL / Kafka / Schema Registry)>
mvn -q verify
# lint the models
bash .zenwave/bin/lint-zdl.sh --strict ../catalog-inventory-api/domain-model.zdl
# export the Hibernate DDL (JPA)
bash .claude/skills/zenwave-test-data/scripts/export-ddl.sh
```

The orchestrator gives the workers the focused command (with the task's `tests:`) and the reviewer the full one.

## Project rules

- Naming conventions (ids, channels, topics): `.claude/skills/zenwave-grill-models/references/zenwave-languages/asyncapi-naming-conventions.md`;
  shared AsyncAPI traits, bindings and error topics in `../api-contract-commons/`.
- Tests: `*Test` run with Surefire (`mvn test`), `*IT` / `*IntegrationTest` with Failsafe (`mvn verify`). Prefer
  in-memory repositories and vertical tests over Mockito; adapters to external resources are tested with
  Testcontainers.
- Generated at build time into `target/` (never edit): OpenAPI interfaces and `*DTO`s, the AsyncAPI SDK.
- Format with `mvn spotless:apply`.
- Decisions already taken that the models don't show: none recorded yet (`docs/adr/`). For a reference
  implementation of the same flow, see `../arcadia-editions-modulith-service-java/docs/zenwave-sdd-overlay.md`
  (Project rules) and its ADRs.
