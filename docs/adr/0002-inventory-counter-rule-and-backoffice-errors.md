# 0002. Inventory counter rule and backoffice errors

Status: accepted, 2026-11-26 (demo: simplest rule that runs; board T-06, T-25)

## Context

The ZDL and the ZFL say which transitions a StockReservation goes through, but not what each one does to the
InventoryPosition counters (`availableQuantity`, `reservedQuantity`, `soldQuantity`), nor what the backoffice
answers on bad input. The fixtures already assume a rule (data.sql A-02).

## Decision

Counters, per reservation line of quantity `q` (A-02):

| Command | available | reserved | sold |
|---|---|---|---|
| `reserveStock` | `-q` | `+q` | |
| `confirmStockReservation` | | `-q` | `+q` |
| `releaseStock` from RESERVED (also `expireReservation`) | `+q` | `-q` | |
| `releaseStock` from CONFIRMED | `+q` | | `-q` |

Backoffice and edge cases (T-25), all "throw, as generated", no special answers:

- `adjustStock` that would take `availableQuantity` below 0: throws.
- unknown sku (`receiveStock`, `adjustStock`): throws (not found).
- `createInventoryPosition` with an existing sku: throws.
- `reserveStock` with an unknown sku: counts as `availableQuantity` 0, so the order is UNAVAILABLE (gate grill G-09).
- `reserveStock` with the same sku on several lines: one reservation per line, availability checked against the sum
  of the lines of that sku (G-10).
- orderId with no reservations: `confirmStockReservation` answers `StockReservationNotHeldResult`; `releaseStock` is a
  no-op without event (same as already RELEASED, ZDL).

## Consequences

The fixture counters (SKU-001 9/0/1, SKU-002 97/3/0, SKU-003 8/1/1) follow this rule. No idempotency or validation
layer beyond what the generated code does (ADR scope: demo).
