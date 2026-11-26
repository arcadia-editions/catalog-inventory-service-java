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
- Events leave through a transactional outbox (Spring Modulith JDBC registry, `event_publication` table, sent to
  Spring Cloud Stream after commit; ADR 0003): Kafka tests must wait for the event, the in-memory producer sees it at once.
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
# compile, and collect MapStruct warnings (log outside target/: `clean` would delete it)
mkdir -p .zenwave/work && mvn -DskipTests clean compile 2>&1 | tee .zenwave/work/compile.log; grep -i "unmapped target" .zenwave/work/compile.log
# focused tests of a task (the implementer runs only these)
mvn -q test -Dtest='<TestClass>'
# full suite with integration tests and containers (once per slice review)
# about 3.5 minutes (217 s, 2026-11-26, with the transactional outbox; 155 s on 2026-11-25); needs Docker (Testcontainers starts the docker-compose PostgreSQL / Kafka / Schema Registry
# on fixed ports 5432, 9092, 8081, 2181: stop other compose stacks first; a leftover stack made a first run fail every IT).
# Surefire failures stop the build before the integration tests: add -Dmaven.test.failure.ignore=true to see both.
# known failing at the baseline (2026-11-25, unchanged 2026-11-26 with the outbox), each fixed by a board task (docs/tasks.md tests:):
#   InventoryApiControllerTest.confirmStockReservationTest, InventoryBackofficeApiControllerTest.receiveStockTest, .adjustStockTest:
#     not implemented (confirm: no reservation loaded, NoSuchElement; receive/adjust: Avro event fields not set) -> T-10, T-27, T-28 (TestDataLoader fixed 2026-11-25: fixtures load, service tests pass)
#   StockReservationRepositoryIntegrationTest.updateTest: asserts Instant.now() -> T-01
#   InventoryApiIntegrationTest (4), InventoryBackofficeApiIntegrationTest (6): no datasource (@EnableDockerCompose commented out) -> T-01 (_400);
#     _201 not implemented -> T-09, T-10, T-26, T-27, T-28
mvn -q verify -Dmaven.test.failure.ignore=true
# lint the models
bash .zenwave/bin/lint-zdl.sh --strict ../catalog-inventory-api/domain-model.zdl
# export the Hibernate DDL (JPA)
bash .claude/skills/zenwave-test-data/scripts/export-ddl.sh -t StockReservationRepositoryIntegrationTest  # -t needed: without it the abstract base boots and nothing is written
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
- Decisions already taken that the models don't show: `docs/adr/` 0001 (core.application uses the generated
  producers), 0002 (counter rule, backoffice errors), 0003 (transactional outbox), 0004 (Confluent Avro wire format);
  gate grill decisions in `docs/gate/place-order-flow-grill.md`. For a reference
  implementation of the same flow, see `../arcadia-editions-modulith-service-java/docs/zenwave-sdd-overlay.md`
  (Project rules) and its ADRs.
