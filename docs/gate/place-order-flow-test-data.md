# place-order-flow: test data review (gate item 13)

Run 2026-11-26 by claude (opus) on behalf of the person. Reviewed the header of
[data.sql](../../src/test/resources/data.sql) (scenario matrix C-01..C-14, coverage, assumptions A-01..A-06) and
the JSON mirrors in `src/test/resources/data/jpa/` against [the board](../tasks.md) and the ZDL/ZFL.

## Checks

| Check | Result |
|---|---|
| Counters follow ADR 0002 (A-02): reserved = sum RESERVED, sold = sum CONFIRMED | OK: SKU-001 9/0/1 (C-01), SKU-002 97/3/0 (C-02 2 + C-11 1), SKU-003 8/1/1 (C-02 1, C-12 1) |
| Totals A-06 (available + reserved + sold) | OK: 10, 100, 10, 0, 1 |
| expiresAt = reservedAt + 10 min (A-03) | OK on all 13 rows |
| State fields per status (confirmedAt on CONFIRMED / was-CONFIRMED, releasedAt + releaseReason on RELEASED, nulls elsewhere) | OK |
| JSON mirrors = SQL rows (13 StockReservation, 5 InventoryPosition, ids 1 + (k-1)*50) | OK, field by field |
| Every chain / fixture named by a board `test data:` exists | OK (C-01..C-14, StockReservation/1.json, InventoryPosition/1.json, SKU-001..SKU-005; SKU-099 is "no row" on purpose) |
| Every ZFL branch of this context has a row or a stated reason | OK: RESERVED/UNAVAILABLE, CONFIRMED/NOT_HELD, NO_PAYMENT/STARTED (stub), 5 release reasons, redeliveries, late fire |
| C-02 before deadline vs fixed now 10:00:00 | OK, expires 10:00:02 |
| C-05 (late authorization) is consistent with the timer releasing only on NO_PAYMENT | OK: payments started after the 10-minute check (S5 path, grill G-07) |

## Fixed

- Header: the fixed Clock and the scheduler now name their tasks (T-03, T-21) instead of "a board task".
- C-12 matrix said "releaseStock from CONFIRMED (PAYMENT_VOIDED)"; the board uses it for PAYMENT_VOID_FAILED
  (T-15, T-17; the only listener until P-01). Now says both.
- Coverage: added the grill edge cases (unknown sku → SKU-099 no row, UNAVAILABLE with 0; same sku on two lines) and
  the outbox (no row, T-29). Board T-12 `test data:` names SKU-099.

No row changed; the counters and states are as approved.
