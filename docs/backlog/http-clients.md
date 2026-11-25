# HTTP clients for synchronous calls

Manual task, not on the flow board (`docs/tasks.md`). Do it before implementing the flow steps that call Payments Processing.

## Why

`place-order-flow.zfl` has steps where this service calls Payments Processing synchronously (`call ...`), and it waits
for the result:

- `expireReservation` calls `getPaymentStatus` (`POST /payments/status`): `NoPaymentResult` or `PaymentStartedResult`.

Today these calls are `TODO`s (or don't exist yet, before generation). The contract is
[`payments-processing-api/openapi.yml`](https://github.com/arcadia-editions/payments-processing-api/blob/main/openapi.yml).

**Blocked on the contract:** `payments-processing-api/openapi.yml` has `paths: {}` today, although its
`domain-model.zdl` declares `@rest("/payments")` and `getPaymentStatus` with `@post("/status")`. Regenerate and
publish that `openapi.yml` (`main`) first.

## What to do

1. **Generate the client interfaces.** Add a second `openapi-generator-maven-plugin` execution to `pom.xml`
   (id `generate-payments-processing-client`), next to the existing one:
   - properties: `<payments-processing.openapi.inputSpec>https://raw.githubusercontent.com/arcadia-editions/payments-processing-api/refs/heads/main/openapi.yml</payments-processing.openapi.inputSpec>`,
     `<paymentsProcessingClientPackage>${basePackage}.clients.payments</paymentsProcessingClientPackage>`;
   - `generatorName` `spring`, `library` `spring-http-interface` (Spring `@HttpExchange` interfaces, no implementation);
   - `apiPackage` `${paymentsProcessingClientPackage}`, `modelPackage` `${paymentsProcessingClientPackage}.dtos`, `modelNameSuffix` `DTO`;
   - `generateSupportingFiles` `false`, `useTags` `true`, `openApiNullable` `false`, `documentationProvider` `none`,
     `useJakartaEe` `true`, same `typeMappings` as the server execution;
   - `operationIds` / `apisToGenerate` limited to what this service calls: `getPaymentStatus`.

   Generated into `target/` like the rest: never edit.

2. **Build the client from the interfaces.** One `@Configuration` in `config` (e.g. `PaymentsProcessingClientConfiguration`)
   that binds `arcadia.clients.payments-processing.*` (already in `application.yml`) and exposes the generated interface as a
   bean:

   ```java
   RestClient restClient = RestClient.builder()
       .baseUrl(props.baseUrl())                      // ${PAYMENTS_PROCESSING_URL:http://localhost:8103/api}
       .requestInterceptor(new BasicAuthenticationInterceptor(props.username(), props.password()))
       .build();
   HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build();
   return factory.createClient(PaymentsProcessingApi (the name depends on the tag).class);
   ```

   (Or Spring Boot 4 `@ImportHttpServices` + `spring.http.serviceclient.payments-processing.base-url`, if preferred; then move
   the properties.)

3. **Adapter for the outbound port.** The core calls Payments Processing through an outbound port in `core.outbound`
   (the generated one, or one the board names). An adapter in `adapters` implements it with the generated interface
   and maps DTOs with MapStruct. Map each response the flow names (`NoPaymentResult`, `PaymentStartedResult`) to the port's result; HTTP errors
   other than those are technical failures.

4. **Tests.** The adapter is tested against a stub of the contract (WireMock, or MockRestServiceServer). The core
   tests keep using an in-memory fake of the port.

## Runtime

| Property | Env var | Local default | docker-compose |
|---|---|---|---|
| `arcadia.clients.payments-processing.base-url` | `PAYMENTS_PROCESSING_URL` | `http://localhost:8103/api` | `http://payments-processing-service:8103/api` |
| `arcadia.clients.payments-processing.username` / `password` | `PAYMENTS_PROCESSING_USERNAME` / `PAYMENTS_PROCESSING_PASSWORD` | `admin` / `password` | same |

Ports of every service: `arcadia-editions-microservices-compose/README.md`.
