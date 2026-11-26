# place-order-flow: projections review (gate item 5)

Run 2026-11-26 by claude (opus) on behalf of the person. Projections of catalog-inventory-api service-all 0.2.0
(working copy = tag `release/service-all/v0.2.0`, clean) reviewed against
[domain-model.zdl](../../../catalog-inventory-api/domain-model.zdl) and
[place-order-flow.zfl](../../../arcadia-editions-architecture/business-flows/place-order-flow.zfl).

## State machine (StockReservation.status, from the ZDL `@transition`s)

```
(none) --reserveStock--> RESERVED --confirmStockReservation--> CONFIRMED
RESERVED  --releaseStock | expireReservation (NO_PAYMENT)--> RELEASED
CONFIRMED --releaseStock--> RELEASED          RELEASED: terminal (release/expire on it = no-op, no event)
```

OK: matches the ZFL order and the redelivery rule. InventoryPosition has no lifecycle (counters per ADR 0002).

## Sequence per branch (catalog side)

| Branch | In | Catalog | Out | OK |
|---|---|---|---|---|
| completed | POST /inventory/reservations/reserve | RESERVED x lines | 201 RESERVED (lines, expiresAt) | OK |
| | POST /inventory/reservations/confirm | CONFIRMED | 201 CONFIRMED + StockReservationConfirmed (no consumer, on purpose) | OK |
| stockGone | POST reserve | nothing | 201 UNAVAILABLE (orders answers 409) | OK |
| cancelled (payment) | PaymentDeclined / RetryExhausted / Voided / VoidFailed | RELEASED, reason = event type | StockReleased → orders cancelOrder | only VoidFailed is in the contract: **P-01** |
| cancelled (expiry) | in-process timer on expiresAt → getPaymentStatus | NO_PAYMENT: RELEASED RESERVATION_EXPIRED; STARTED: nothing | StockReleased | OK (timer: grill G-05) |
| late authorization | POST confirm after expiry | nothing | 201 NOT_HELD → orders OrderConfirmationRejected → void → PaymentVoided/VoidFailed → release no-op | OK |

## Documents

| Projection | Finding | Decision |
|---|---|---|
| SUMMARY.md | Scope says "release reserved stock when payment or scheduling fails"; confirm and release-after-confirm are not named | OK as a summary, no change |
| EVENT_CATALOG.md | Business narrative only, no event list; nothing contradicts the ZDL | OK |
| asyncapi.yml | 4 send operations / channels, addresses = ZDL `topic`s; payloads Avro with CommonHeaders trait | OK |
| asyncapi.yml | `defaultContentType: application/json` while payloads are Avro | Harmless: native Confluent encoding (ADR 0004) ignores it |
| asyncapi.yml | Operation names `onStockReleased`, ... become the outbox binding names `on-stock-released-out` | Recorded in ADR 0003, T-08 |
| avro/*.avsc | Instant → `string`; `reason` enum non-null in StockReleased | ISO-8601 UTC (grill G-20); reason always set |
| asyncapi-client.yml | Only `payment-void-failed-event-v1` of the 4 releaseStock triggers | **P-01** open: SDK fixed, contract regeneration + release 0.2.1 pending; blocks T-16, T-18 only (item 3) |
| asyncapi-client.yml | `$ref`s to `payments-processing-api/main` and `api-contract-commons/main` | Known residual coupling (overlay) |
| openapi.yml | reserve / confirm / positions / stock-intakes / adjustments, all 201 + 400; result wrappers with `outcome` | OK; thrown errors (ADR 0002) go through the generated exception translator, not in the contract (demo) |
| openapi.yml | `sku` both path and required body field in receive / adjust | Path wins (grill G-21) |
| openapi.yml | No REST for releaseStock / expireReservation | OK: event / timer driven |

No new proposal. Open: P-01 (allowed by item 3).
