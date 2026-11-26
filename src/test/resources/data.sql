-- Scenario test data. Regenerate with $zenwave-test-scenarios (place-order-flow).
-- DDL exported 2026-11-25 to target/ddl/schema.sql (models: catalog-inventory service-all 0.2.0). Aggregates: StockReservation
-- (stock_reservation), InventoryPosition (inventory_position).
-- Fixed test "now" (fixed Clock, board task T-03): 2026-10-14T10:00:00Z. Reservation window: 10 minutes (ZFL).
-- JSON mirrors: src/test/resources/data/jpa/<Entity>/<k>.json, k = row position in its table, id = 1 + (k-1)*50.
-- Business ids shared with orders-checkout fixtures: ORD-00n, SKU-00n (same chain ids C-01..C-10).
--
-- SCENARIO MATRIX: chain | business ids | StockReservation state per line (upstream state implied) | used by
-- C-01 | ORD-001, SKU-001x1 | CONFIRMED (order CONFIRMED, flow completed) | confirmStockReservation redelivery (CONFIRMED, no change); repository tests findById(1)
-- C-02 | ORD-002, SKU-002x2 + SKU-003x1 | RESERVED x2, expires 10:00:02 (before deadline) | confirmStockReservation CONFIRMED; releaseStock from RESERVED; expireReservation before deadline; multiplicity
-- C-03 | ORD-003, SKU-002x1 | RELEASED PAYMENT_DECLINED (order CREATED, cancel pending) | releaseStock redelivery no-op; expireReservation late fire no-op
-- C-04 | ORD-004, SKU-002x1 | RELEASED PAYMENT_VOIDED, was CONFIRMED | releaseStock from CONFIRMED (result state); outcome voided after confirmation
-- C-05 | ORD-005, SKU-002x1 | RELEASED RESERVATION_EXPIRED (late authorization) | confirmStockReservation NOT_HELD
-- C-06 | ORD-006, SKU-002x1 | RELEASED RESERVATION_EXPIRED (order CANCELLED) | outcome cancelled by expiry; expireReservation redelivery no-op
-- C-07 | ORD-007, SKU-002x1 | RELEASED PAYMENT_DECLINED (order CANCELLED) | outcome cancelled by decline
-- C-08 | ORD-008, SKU-002x1 | RELEASED PAYMENT_RETRY_EXHAUSTED (order CANCELLED) | outcome cancelled by retries exhausted
-- C-09 | ORD-009, SKU-002x1 | RELEASED PAYMENT_VOIDED, was CONFIRMED (order CANCELLED) | outcome cancelled after fulfillment / capture failure
-- C-10 | ORD-010, SKU-002x1 | RELEASED PAYMENT_VOID_FAILED, was CONFIRMED (order CANCELLED) | outcome cancelled, void refused
-- C-11 | ORD-011, SKU-002x1 | RESERVED, expired 09:55:11 (past deadline) | expireReservation expired (NO_PAYMENT, stub-driven) / payment started (stub-driven); off-chain (no order fixture; catalog never reads orders)
-- C-12 | ORD-012, SKU-003x1 | CONFIRMED (order CONFIRMED, fulfillment pending) | releaseStock from CONFIRMED (PAYMENT_VOID_FAILED today, T-15/T-17; PAYMENT_VOIDED once P-01 is released, T-18); off-chain
-- C-13 | SKU-004 | InventoryPosition 0 available | reserveStock UNAVAILABLE (whole order); same SKU-004 as orders' stock-unavailable stub
-- C-14 | SKU-005 | InventoryPosition 1 available | reserveStock UNAVAILABLE with less than requested (request 2); mixed order SKU-002 + SKU-005
--
-- COVERAGE (need -> chain/row, or why no row)
-- Lifecycle: reserveStock (to RESERVED) -> C-02/C-13/C-14 positions; releaseStock from RESERVED -> C-02, C-11, from CONFIRMED -> C-12;
--   expireReservation from RESERVED -> C-11; confirmStockReservation from RESERVED -> C-02
-- Branches: reserveStock RESERVED -> SKU-002 (97 available), UNAVAILABLE -> C-13, C-14; confirmStockReservation CONFIRMED -> C-02, NOT_HELD -> C-05;
--   expireReservation NO_PAYMENT / STARTED -> C-11 (stub-driven: payments getPaymentStatus decides)
-- releaseStock reasons (event type sets it): PAYMENT_DECLINED C-03/C-07, PAYMENT_RETRY_EXHAUSTED C-08, PAYMENT_VOIDED C-04/C-09,
--   PAYMENT_VOID_FAILED C-10, RESERVATION_EXPIRED C-05/C-06. Only payment-void-failed is in asyncapi-client.yml 0.2.0 (P-01)
-- Outcomes: completed C-01; cancelled by decline C-07, retries exhausted C-08, voided C-09, void refused C-10, expiry C-06;
--   stockGone -> no reservation row (C-13 position); notificationFailed -> no row (notifications context)
-- Multiplicity: one reservation per order line -> C-02 (2 lines)
-- Time: expiresAt = reservedAt + 10 min; past deadline C-11, before deadline C-02. the timer trigger is an in-process scheduler on expiresAt (T-21)
-- Redelivery: confirmStockReservation on CONFIRMED -> C-01; releaseStock on RELEASED -> C-03; expireReservation with no RESERVED line -> C-03/C-06;
--   reserveStock redelivery for an orderId already reserved -> throws (T-07 decided), C-02 usable
-- Edge (ADR 0002): reserveStock unknown sku -> no row (e.g. SKU-099), UNAVAILABLE with availableQuantity 0; same sku on two lines -> SKU-002 (97)
-- Outbox (ADR 0003, T-29): no row; event_publication is created empty by Spring Modulith
-- Backoffice: createInventoryPosition new sku -> no row (e.g. SKU-099), duplicate -> SKU-001 (unique); receiveStock / adjustStock -> SKU-001
-- Read side: no query endpoint in openapi.yml 0.2.0; InventoryPosition returned by the backoffice POSTs
--
-- ASSUMPTIONS: assumption | value used | open question
-- A-01 reservationId format | RSV-<order number>-<line n> | fixture ids only; the service generates a UUIDv7 (T-05, decided 2026-11-25)
-- A-02 counters: reserved = sum of RESERVED quantities, sold = sum of CONFIRMED, release returns quantity to available | SKU-001 9/0/1, SKU-002 97/3/0, SKU-003 8/1/1 | decided T-06, ADR 0002
-- A-03 expiresAt = reservedAt + 10 minutes | ZFL @time("10 minutes after OrderCreated") | -
-- A-04 reservedAt = order createdAt of the orders fixture, except C-05 (09:40:05): orders' ORD-005 created 09:50:05 cannot have been released by the timer before "now" | as listed | reported to orders
-- A-05 releasedAt ~1 s before the order's cancelledAt; confirmedAt = order confirmedAt | as listed | -
-- A-06 received stock totals (available+reserved+sold): SKU-001 10, SKU-002 100, SKU-003 10, SKU-004 0, SKU-005 1 | as listed | -

-- Catalog Inventory: inventory_position (backoffice)
INSERT INTO inventory_position (id, version, sku, available_quantity, reserved_quantity, sold_quantity)
VALUES (nextval('inventory_position_seq'), 0, 'SKU-001', 9, 0, 1); -- C-01
INSERT INTO inventory_position (id, version, sku, available_quantity, reserved_quantity, sold_quantity)
VALUES (nextval('inventory_position_seq'), 0, 'SKU-002', 97, 3, 0); -- C-02..C-11
INSERT INTO inventory_position (id, version, sku, available_quantity, reserved_quantity, sold_quantity)
VALUES (nextval('inventory_position_seq'), 0, 'SKU-003', 8, 1, 1); -- C-02, C-12
INSERT INTO inventory_position (id, version, sku, available_quantity, reserved_quantity, sold_quantity)
VALUES (nextval('inventory_position_seq'), 0, 'SKU-004', 0, 0, 0); -- C-13
INSERT INTO inventory_position (id, version, sku, available_quantity, reserved_quantity, sold_quantity)
VALUES (nextval('inventory_position_seq'), 0, 'SKU-005', 1, 0, 0); -- C-14

-- Catalog Inventory: stock_reservation (one per order line)
INSERT INTO stock_reservation (id, version, reservation_id, order_id, sku, quantity, status, reserved_at, expires_at, confirmed_at, released_at, release_reason)
VALUES (nextval('stock_reservation_seq'), 0, 'RSV-001-1', 'ORD-001', 'SKU-001', 1, 'CONFIRMED', '2026-09-26T13:00:00Z', '2026-09-26T13:10:00Z', '2026-09-26T13:05:00Z', NULL, NULL); -- C-01
INSERT INTO stock_reservation (id, version, reservation_id, order_id, sku, quantity, status, reserved_at, expires_at, confirmed_at, released_at, release_reason)
VALUES (nextval('stock_reservation_seq'), 0, 'RSV-002-1', 'ORD-002', 'SKU-002', 2, 'RESERVED', '2026-10-14T09:50:02Z', '2026-10-14T10:00:02Z', NULL, NULL, NULL); -- C-02
INSERT INTO stock_reservation (id, version, reservation_id, order_id, sku, quantity, status, reserved_at, expires_at, confirmed_at, released_at, release_reason)
VALUES (nextval('stock_reservation_seq'), 0, 'RSV-002-2', 'ORD-002', 'SKU-003', 1, 'RESERVED', '2026-10-14T09:50:02Z', '2026-10-14T10:00:02Z', NULL, NULL, NULL); -- C-02
INSERT INTO stock_reservation (id, version, reservation_id, order_id, sku, quantity, status, reserved_at, expires_at, confirmed_at, released_at, release_reason)
VALUES (nextval('stock_reservation_seq'), 0, 'RSV-003-1', 'ORD-003', 'SKU-002', 1, 'RELEASED', '2026-10-14T09:50:03Z', '2026-10-14T10:00:03Z', NULL, '2026-10-14T09:51:03Z', 'PAYMENT_DECLINED'); -- C-03
INSERT INTO stock_reservation (id, version, reservation_id, order_id, sku, quantity, status, reserved_at, expires_at, confirmed_at, released_at, release_reason)
VALUES (nextval('stock_reservation_seq'), 0, 'RSV-004-1', 'ORD-004', 'SKU-002', 1, 'RELEASED', '2026-10-14T09:50:04Z', '2026-10-14T10:00:04Z', '2026-10-14T09:50:34Z', '2026-10-14T09:51:34Z', 'PAYMENT_VOIDED'); -- C-04
INSERT INTO stock_reservation (id, version, reservation_id, order_id, sku, quantity, status, reserved_at, expires_at, confirmed_at, released_at, release_reason)
VALUES (nextval('stock_reservation_seq'), 0, 'RSV-005-1', 'ORD-005', 'SKU-002', 1, 'RELEASED', '2026-10-14T09:40:05Z', '2026-10-14T09:50:05Z', NULL, '2026-10-14T09:50:05Z', 'RESERVATION_EXPIRED'); -- C-05
INSERT INTO stock_reservation (id, version, reservation_id, order_id, sku, quantity, status, reserved_at, expires_at, confirmed_at, released_at, release_reason)
VALUES (nextval('stock_reservation_seq'), 0, 'RSV-006-1', 'ORD-006', 'SKU-002', 1, 'RELEASED', '2026-10-14T09:40:06Z', '2026-10-14T09:50:06Z', NULL, '2026-10-14T09:50:06Z', 'RESERVATION_EXPIRED'); -- C-06
INSERT INTO stock_reservation (id, version, reservation_id, order_id, sku, quantity, status, reserved_at, expires_at, confirmed_at, released_at, release_reason)
VALUES (nextval('stock_reservation_seq'), 0, 'RSV-007-1', 'ORD-007', 'SKU-002', 1, 'RELEASED', '2026-10-14T09:50:07Z', '2026-10-14T10:00:07Z', NULL, '2026-10-14T09:51:06Z', 'PAYMENT_DECLINED'); -- C-07
INSERT INTO stock_reservation (id, version, reservation_id, order_id, sku, quantity, status, reserved_at, expires_at, confirmed_at, released_at, release_reason)
VALUES (nextval('stock_reservation_seq'), 0, 'RSV-008-1', 'ORD-008', 'SKU-002', 1, 'RELEASED', '2026-10-14T09:50:08Z', '2026-10-14T10:00:08Z', NULL, '2026-10-14T09:51:27Z', 'PAYMENT_RETRY_EXHAUSTED'); -- C-08
INSERT INTO stock_reservation (id, version, reservation_id, order_id, sku, quantity, status, reserved_at, expires_at, confirmed_at, released_at, release_reason)
VALUES (nextval('stock_reservation_seq'), 0, 'RSV-009-1', 'ORD-009', 'SKU-002', 1, 'RELEASED', '2026-10-14T09:50:09Z', '2026-10-14T10:00:09Z', '2026-10-14T09:50:39Z', '2026-10-14T09:51:38Z', 'PAYMENT_VOIDED'); -- C-09
INSERT INTO stock_reservation (id, version, reservation_id, order_id, sku, quantity, status, reserved_at, expires_at, confirmed_at, released_at, release_reason)
VALUES (nextval('stock_reservation_seq'), 0, 'RSV-010-1', 'ORD-010', 'SKU-002', 1, 'RELEASED', '2026-10-14T09:50:10Z', '2026-10-14T10:00:10Z', '2026-10-14T09:50:40Z', '2026-10-14T09:51:39Z', 'PAYMENT_VOID_FAILED'); -- C-10
INSERT INTO stock_reservation (id, version, reservation_id, order_id, sku, quantity, status, reserved_at, expires_at, confirmed_at, released_at, release_reason)
VALUES (nextval('stock_reservation_seq'), 0, 'RSV-011-1', 'ORD-011', 'SKU-002', 1, 'RESERVED', '2026-10-14T09:45:11Z', '2026-10-14T09:55:11Z', NULL, NULL, NULL); -- C-11
INSERT INTO stock_reservation (id, version, reservation_id, order_id, sku, quantity, status, reserved_at, expires_at, confirmed_at, released_at, release_reason)
VALUES (nextval('stock_reservation_seq'), 0, 'RSV-012-1', 'ORD-012', 'SKU-003', 1, 'CONFIRMED', '2026-10-14T09:50:12Z', '2026-10-14T10:00:12Z', '2026-10-14T09:50:42Z', NULL, NULL); -- C-12
