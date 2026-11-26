# place-order-flow: grill of the models (gate item 4)

Run 2026-11-26 by claude (opus) on behalf of the person (HITL review delegated; demo: simplest option that runs).
Skill `zenwave-grill-models`. Scope: the CatalogInventory steps of the flow.

Models: [domain-model.zdl](../../../catalog-inventory-api/domain-model.zdl) (service-all 0.2.0),
[place-order-flow.zfl](../../../arcadia-editions-architecture/business-flows/place-order-flow.zfl),
[payments domain-model.zdl](../../../payments-processing-api/domain-model.zdl),
[orders domain-model.zdl](../../../orders-checkout-api/domain-model.zdl). No `GLOSSARY.md` / `docs/domain.md` exist.

## Deterministic checks

- Lint: `bash .zenwave/bin/lint-zdl.sh --strict ../catalog-inventory-api/domain-model.zdl` → `OK`, no warnings.
- Identities: all 5 `@zdl(...)` paths resolve; every command, event and result of the ZFL exists in its ZDL (scripted
  grep over the 5 ZDLs, no miss); every `@compensates(x)` names a command (authorizePayment, reserveStock,
  startOrderCheckout, scheduleFulfillment); `systems` match the services.

## Findings → decision

| # | Finding (lines) | Decision |
|---|---|---|
| G-01 | Correlation: reservation is per line ([zdl:56-68](../../../catalog-inventory-api/domain-model.zdl#L56)), all commands take `orderId` | As designed: repositories find by orderId (T-04); every command acts on all lines of the order |
| G-02 | Lineage `ReserveStockInput.orderId` / items | From orders `startOrderCheckout` (orders generates the id before the call, orders ADR 0001); items from StartOrderCheckout. OK |
| G-03 | Lineage `ReleaseStockInput.reason` ([zdl:100-101](../../../catalog-inventory-api/domain-model.zdl#L100)) | From the listener's event type (T-15, T-16). OK |
| G-04 | releaseStock has 4 `@asyncapi` triggers, released `asyncapi-client.yml` only 1 ([zdl:174-177](../../../catalog-inventory-api/domain-model.zdl#L174)) | **P-01** (open, blocks T-16, T-18 only) |
| G-05 | Timer: ZFL `@time("10 minutes after OrderCreated")` ([zfl:275](../../../arcadia-editions-architecture/business-flows/place-order-flow.zfl#L275)) but catalog consumes no OrderCreated; ZDL `@timer(api: SchedulerApi)` ([zdl:186](../../../catalog-inventory-api/domain-model.zdl#L186)) names an api not in `apis {}` | As designed for the demo: in-process scheduler on `StockReservation.expiresAt = reservedAt + 10 min` (T-21, A-03). reservedAt precedes OrderCreated by the HTTP round trip only. No proposal: lint is clean, nothing is generated from `SchedulerApi` |
| G-06 | Timer stop | Implicit: the scheduler only selects RESERVED lines past expiresAt; confirm/release move them out. Late fire: no-op (ZDL doc, T-20, C-03/C-06) |
| G-07 | Race timer vs late PaymentAuthorized at minute 10 | As designed: timer asks payments; STARTED → nothing. Only NO_PAYMENT then authorization (payments read OrderCreated late) reaches NOT_HELD → S5 (C-05). Generated `@Version` on both aggregates turns a true concurrent update into an optimistic-lock error (retried by Kafka / next tick) |
| G-08 | Two orders reserving the last copy at once (SKU-005) | As designed: `@Version` on InventoryPosition; the loser gets an optimistic-lock exception (500 to orders, its checkout fails). No retry layer (demo) |
| G-09 | reserveStock with an unknown sku (no InventoryPosition) — ADR 0002 covers receive/adjust only | Decided: counts as `availableQuantity 0` → UNAVAILABLE line (customer gets the stockGone path, not a 500). Added to ADR 0002 and T-12 |
| G-10 | reserveStock with the same sku on two lines | Decided: allowed, one reservation per line; availability is checked per sku against the sum of the order's lines for that sku (no oversell). Added to T-09, T-12 |
| G-11 | reserveStock redelivery / orderId with reservations | Decided earlier: throws (T-07) |
| G-12 | confirm redelivery answer ([zdl:191](../../../catalog-inventory-api/domain-model.zdl#L191)) | CONFIRMED with the stored `confirmedAt` of the lines, no event (T-10, C-01) |
| G-13 | confirm / release for an orderId with no reservations | Decided earlier: NOT_HELD / no-op (ADR 0002) |
| G-14 | Counters per transition (not in the models) | Decided earlier: ADR 0002 (A-02) |
| G-15 | StockReleased: one per order vs one per reservation (generated) | One per order with its items (T-14), matches the event shape ([zdl:197-207](../../../catalog-inventory-api/domain-model.zdl#L197)) and orders' single cancelOrder |
| G-16 | Handled: StockReleased → orders cancelOrder; StockReservationConfirmed published on purpose ([zdl:209](../../../catalog-inventory-api/domain-model.zdl#L209)); StockReceived / StockAdjusted outside the flow | OK |
| G-17 | Lifecycle: RESERVED→CONFIRMED (confirm), RESERVED→RELEASED (release, expire), CONFIRMED→RELEASED (release); RELEASED terminal | OK, all reachable, `from` states agree with the ZFL order |
| G-18 | Ownership: catalog only mutates its two aggregates; getPaymentStatus is a read | OK |
| G-19 | Outage of payments getPaymentStatus during expire | As designed: expire throws, the lines stay RESERVED, next scheduler tick retries (T-20/T-21) |
| G-20 | Contracts: Instant fields are Avro `string` ([avro/StockReleased.avsc](../../../catalog-inventory-api/avro/StockReleased.avsc)) | ISO-8601 UTC (`Instant.toString()`) in every event |
| G-21 | Contracts: `receiveStock` / `adjustStock` take `sku` in the path and required in the body | Path sku wins, body sku ignored (T-27, T-28 already say "load by path sku") |
| G-22 | Wire format: orders (its ADR 0002) and payments read/write Confluent `KafkaAvroSerializer`; this pom has the SCS Avro converter | Decided: Confluent wire format, String keys, native encoding: [ADR 0004](../adr/0004-avro-wire-format-confluent.md); folded into T-08 |
| G-23 | Outbox: events leave after commit (ADR 0003) | Implemented before AFK; the generated binding names become `on-<operation>-out` (T-08 updated). The rollback/commit test is board task T-29 |
| G-24 | Language: no GLOSSARY.md | As designed (demo): ZDL doc comments and SUMMARY.md are the vocabulary; same in the sibling services |
| G-25 | Test data: every outcome representable | Yes, see [place-order-flow-test-data.md](place-order-flow-test-data.md) |

## Frontier

Empty: every finding is decided above or is P-01. New proposals: none. ADRs written: 0004; ADR 0002 and 0003 amended.
