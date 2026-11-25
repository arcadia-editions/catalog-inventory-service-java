# PlaceOrderFlow: tasks

flow: ../arcadia-editions-architecture/business-flows/place-order-flow.zfl
baseline:
branch: afk/place-order-flow

<!--
Scope: the CatalogInventory bounded context (this repository) only. The steps of the other contexts (orders,
payments, fulfillment, notifications) are boards of their own repositories; here they are stubs, HTTP calls or Kafka messages.
Model paths: ZFL = ../arcadia-editions-architecture/business-flows/place-order-flow.zfl,
ZDL = ../catalog-inventory-api/domain-model.zdl, payments ZDL = ../payments-processing-api/domain-model.zdl.
Test data chains C-01..C-14: header of src/test/resources/data.sql (assumptions A-01..A-06 there).
Approved models: catalog-inventory-api service-all 0.2.0 (tag release/service-all/v0.2.0); ZFL at
arcadia-editions-architecture tag baseline/place-order-flow-<n>.
Open proposal: P-01 (asyncapi-client.yml 0.2.0 consumes only payment-void-failed) blocks T-16, T-18.
The backoffice commands are outside place-order-flow: slice S6 only because their generated tests fail at the baseline.
-->

## S0 Baseline
review: [ ]

- [ ] T-01 mechanical AFK Generated tests that should pass do: TestDataLoader reads data/jpa/<Entity simple name> (it reads the table name today, so no fixture loads), updateTest without Instant.now(), API ITs with a datasource (@EnableDockerCompose in BaseWebTestClientTest)
      test data: data/jpa/StockReservation/1.json, data/jpa/InventoryPosition/1.json, data.sql (C-01, id 1)
      tests: InventoryServiceTest, InventoryBackofficeServiceTest, InventoryApiControllerTest, InventoryBackofficeApiControllerTest, StockReservationRepositoryIntegrationTest#updateTest, InventoryApiIntegrationTest#testReserveStock_400, InventoryApiIntegrationTest#testConfirmStockReservation_400, InventoryBackofficeApiIntegrationTest#testCreateInventoryPosition_400, InventoryBackofficeApiIntegrationTest#testReceiveStock_400, InventoryBackofficeApiIntegrationTest#testAdjustStock_400
- [ ] T-02 bounded HITL ArchitectureTest fails: core.application uses the generated producers in asyncapi.provider (outside the hexagon, as zenwave-scripts.zw sets it). Decide: allow the dependency in the rule, or put an outbound port in core.outbound; record it as an ADR
      tests: ArchitectureTest
- [ ] T-03 mechanical AFK Inject a java.time.Clock (system clock in main, fixed 2026-10-14T10:00:00Z in tests) for every timestamp the services set
      tests: InventoryServiceTest
      blocked by: T-01
- [ ] T-04 mechanical AFK StockReservationRepository.findByOrderId and InventoryPositionRepository.findBySku (JPA and in-memory twins): commands correlate by orderId and sku
      model: ../catalog-inventory-api/domain-model.zdl:56-67, ../catalog-inventory-api/domain-model.zdl:224-229
      test data: data.sql (C-02, C-13)
      tests: StockReservationRepositoryIntegrationTest, InventoryPositionRepositoryIntegrationTest
- [ ] T-05 bounded HITL Decide how reservationId is generated (format, uniqueness) and record it as an ADR; the ZDL and ZFL do not say (fixtures assume RSV-<order>-<line>, A-01)
      model: ../catalog-inventory-api/domain-model.zdl:57, ../catalog-inventory-api/domain-model.zdl:146-155
- [ ] T-06 bounded HITL Decide the inventory counter rule: what reserveStock, confirmStockReservation, releaseStock (from RESERVED and from CONFIRMED) and expireReservation do to availableQuantity, reservedQuantity, soldQuantity; record it as an ADR (fixtures assume A-02)
      model: ../catalog-inventory-api/domain-model.zdl:224-229, ../arcadia-editions-architecture/business-flows/place-order-flow.zfl:94-105
      test data: data.sql (SKU-001..SKU-003 counters)
- [ ] T-07 bounded HITL Decide what reserveStock does when the orderId already has reservations (a retried HTTP call): the ZFL redelivery rule covers policies, not this synchronous call
      model: ../catalog-inventory-api/domain-model.zdl:169-171, ../arcadia-editions-architecture/business-flows/place-order-flow.zfl:62-64
      test data: data.sql (C-02)
- [ ] T-08 mechanical AFK Spring Cloud Stream bindings in application.yml for the produced channels (stock-released, stock-reservation-confirmed, stock-received, stock-adjusted) and the consumed payment-void-failed-event-v1, replacing the archetype's on-customer-event
      model: ../catalog-inventory-api/domain-model.zdl:197, ../catalog-inventory-api/domain-model.zdl:210, ../catalog-inventory-api/domain-model.zdl:271, ../catalog-inventory-api/domain-model.zdl:283, ../catalog-inventory-api/domain-model.zdl:177
      tests: InventoryApiIntegrationTest
      blocked by: T-01

## S1 completed (tracer bullet)
path: StartOrderCheckout > startOrderCheckout > reserveStock[StockReservedResult] > OrderCreated
      > authorizePayment[authorized] > PaymentAuthorized > confirmOrder > confirmStockReservation[confirmed]
      > StockReservationConfirmed > OrderConfirmed > ... > end completed
review: [ ]

- [ ] T-09 bounded AFK reserveStock reserved: every line has the stock, one RESERVED reservation per line (reservationId per T-05, reservedAt from the Clock, expiresAt = reservedAt + 10 min), counters per T-06, persist, answer RESERVED with the lines and expiresAt
      model: ../arcadia-editions-architecture/business-flows/place-order-flow.zfl:94-105, ../catalog-inventory-api/domain-model.zdl:56-67, ../catalog-inventory-api/domain-model.zdl:84-90, ../catalog-inventory-api/domain-model.zdl:131-155, ../catalog-inventory-api/domain-model.zdl:169-171
      test data: data.sql (C-02 for the shape of a reservation; SKU-002, SKU-003 positions)
      tests: InventoryServiceTest#reserveStockTest, InventoryApiControllerTest#reserveStockTest, InventoryApiIntegrationTest#testReserveStock_201
      blocked by: T-01, T-03, T-04, T-05, T-06, T-07
- [ ] T-10 bounded AFK confirmStockReservation confirmed: load by orderId, every line RESERVED to CONFIRMED + confirmedAt, counters per T-06, persist, emit one StockReservationConfirmed with the items; every line already CONFIRMED answers CONFIRMED with no change and no event (redelivery)
      model: ../arcadia-editions-architecture/business-flows/place-order-flow.zfl:166-176, ../catalog-inventory-api/domain-model.zdl:93-95, ../catalog-inventory-api/domain-model.zdl:109-124, ../catalog-inventory-api/domain-model.zdl:191-194, ../catalog-inventory-api/domain-model.zdl:210-219
      test data: data.sql (C-02 confirmed, C-01 redelivery)
      tests: InventoryServiceTest#confirmStockReservationTest, InventoryApiControllerTest#confirmStockReservationTest, InventoryApiIntegrationTest#testConfirmStockReservation_201
      blocked by: T-01, T-03, T-04, T-06
- [ ] T-11 bounded AFK Flow test completed (catalog side): POST reserve answers RESERVED and moves the counters; POST confirm answers CONFIRMED and publishes StockReservationConfirmed on catalog-inventory.stock-reservation-confirmed.event.avro.v1
      model: ../arcadia-editions-architecture/business-flows/place-order-flow.zfl:94-105, ../arcadia-editions-architecture/business-flows/place-order-flow.zfl:166-176, ../arcadia-editions-architecture/business-flows/place-order-flow.zfl:340-342
      test data: data.sql (C-01, C-02)
      tests: PlaceOrderFlowCompletedTest
      blocked by: T-08, T-09, T-10

## S2 stockGone
path: StartOrderCheckout > startOrderCheckout > reserveStock[StockUnavailableResult] > StockUnavailable
      > sendStockUnavailableNotification > end stockGone
review: [ ]

- [ ] T-12 bounded AFK reserveStock unavailable: when some line lacks stock, create no reservation and change no counter; answer UNAVAILABLE with every short line (sku, requestedQuantity, availableQuantity)
      model: ../arcadia-editions-architecture/business-flows/place-order-flow.zfl:94-105, ../catalog-inventory-api/domain-model.zdl:131-144, ../catalog-inventory-api/domain-model.zdl:157-164
      test data: data.sql (C-13 SKU-004 none available, C-14 SKU-005 less than requested, mixed with SKU-002)
      tests: InventoryServiceTest#reserveStockUnavailableTest, InventoryApiIntegrationTest#testReserveStockUnavailable
      blocked by: T-09
- [ ] T-13 bounded AFK Flow test stockGone (catalog side): POST reserve with SKU-004 answers UNAVAILABLE, no StockReservation row, counters unchanged
      model: ../arcadia-editions-architecture/business-flows/place-order-flow.zfl:340-343
      test data: data.sql (C-13, C-14)
      tests: PlaceOrderFlowStockGoneTest
      blocked by: T-12

## S3 orderCancelled (payment compensation)
path: ... > PaymentDeclined | PaymentRetryExhausted | PaymentVoided | PaymentVoidFailed > releaseStock > StockReleased
      > cancelOrder > OrderCancelled > sendOrderCancelledNotification > end orderCancelled
review: [ ]

- [ ] T-14 bounded AFK releaseStock: load by orderId, RESERVED or CONFIRMED lines to RELEASED + releasedAt + releaseReason = input.reason, counters per T-06, persist, emit one StockReleased per order with its items (not one per reservation, as generated); no-op, no event, when already RELEASED
      model: ../arcadia-editions-architecture/business-flows/place-order-flow.zfl:255-266, ../catalog-inventory-api/domain-model.zdl:98-102, ../catalog-inventory-api/domain-model.zdl:173-179, ../catalog-inventory-api/domain-model.zdl:197-207
      test data: data.sql (C-02 from RESERVED, C-12 from CONFIRMED, C-03 redelivery)
      tests: InventoryServiceTest#releaseStockTest
      blocked by: T-01, T-03, T-04, T-06
- [ ] T-15 mechanical AFK PaymentVoidFailedConsumerService: map PaymentVoidFailed.orderId to ReleaseStockInput with reason PAYMENT_VOID_FAILED (from the event type, never its free-text reason) and call releaseStock
      model: ../catalog-inventory-api/domain-model.zdl:98-102, ../catalog-inventory-api/domain-model.zdl:177, ../arcadia-editions-architecture/business-flows/place-order-flow.zfl:255-266
      test data: data.sql (C-12)
      tests: PaymentVoidFailedConsumerServiceTest
      blocked by: T-08, T-14
- [!] T-16 mechanical AFK Consumers for PaymentDeclined, PaymentRetryExhausted, PaymentVoided: reason PAYMENT_DECLINED, PAYMENT_RETRY_EXHAUSTED, PAYMENT_VOIDED, call releaseStock
      model: ../catalog-inventory-api/domain-model.zdl:174-176, ../arcadia-editions-architecture/business-flows/place-order-flow.zfl:263
      test data: data.sql (C-02, C-12)
      tests: PaymentDeclinedConsumerServiceTest, PaymentRetryExhaustedConsumerServiceTest, PaymentVoidedConsumerServiceTest
      blocked by: T-14
      blocked: P-01
- [ ] T-17 bounded AFK Flow test orderCancelled, void refused (catalog side): a PaymentVoidFailed message releases a CONFIRMED order (C-12 shape) and publishes StockReleased PAYMENT_VOID_FAILED; redelivered, it publishes nothing
      model: ../arcadia-editions-architecture/business-flows/place-order-flow.zfl:255-266, ../arcadia-editions-architecture/business-flows/place-order-flow.zfl:340-344
      test data: data.sql (C-10, C-12, C-03)
      tests: PlaceOrderFlowCancelledTest#voidFailed
      blocked by: T-15
- [!] T-18 bounded AFK Flow test orderCancelled, other causes (catalog side): PaymentDeclined, PaymentRetryExhausted (from RESERVED), PaymentVoided (from CONFIRMED) publish StockReleased with their reason
      model: ../arcadia-editions-architecture/business-flows/place-order-flow.zfl:255-266, ../arcadia-editions-architecture/business-flows/place-order-flow.zfl:340-344
      test data: data.sql (C-02, C-04, C-07, C-08, C-09, C-12)
      tests: PlaceOrderFlowCancelledTest
      blocked by: T-16, T-17
      blocked: P-01

## S4 orderCancelled (reservation expired)
path: ReservationExpired (10 minutes after OrderCreated) > expireReservation > getPaymentStatus[NoPaymentResult]
      > StockReleased > cancelOrder > OrderCancelled > end orderCancelled; getPaymentStatus[PaymentStartedResult]: no release
review: [ ]

- [ ] T-19 advanced AFK Outbound port for payments getPaymentStatus (POST /payments/status) with its REST client generated from payments-processing service-all 0.2.0 openapi.yml (not main; see docs/backlog/http-clients.md), and an in-memory stub for tests (NO_PAYMENT / STARTED by orderId)
      model: ../catalog-inventory-api/domain-model.zdl:187, ../payments-processing-api/domain-model.zdl:196-198, ../arcadia-editions-architecture/business-flows/place-order-flow.zfl:293-297
      tests: PaymentsProcessingClientStubTest
      blocked by: T-01
- [ ] T-20 bounded AFK expireReservation: load the RESERVED lines of the order; none: no-op, no event (late fire); call getPaymentStatus; NO_PAYMENT: RELEASED RESERVATION_EXPIRED + releasedAt, counters per T-06, persist, one StockReleased; STARTED: nothing
      model: ../arcadia-editions-architecture/business-flows/place-order-flow.zfl:268-297, ../catalog-inventory-api/domain-model.zdl:105-107, ../catalog-inventory-api/domain-model.zdl:181-189
      test data: data.sql (C-11 past deadline, C-03 and C-06 late fire)
      tests: InventoryServiceTest#expireReservationTest
      blocked by: T-14, T-19
- [ ] T-21 advanced AFK ReservationExpired timer: a scheduler on StockReservation.expiresAt with the injected Clock that runs expireReservation once per order whose RESERVED lines are past their deadline (channel reservation-expired-event-v1 as the ZDL @timer names it), disabled in tests by a property; same shape as the modulith's ReservationExpiredScheduler
      model: ../arcadia-editions-architecture/business-flows/place-order-flow.zfl:268-278, ../catalog-inventory-api/domain-model.zdl:181-189
      test data: data.sql (C-11 past deadline, C-02 before deadline)
      tests: ReservationExpiredSchedulerTest
      blocked by: T-20
- [ ] T-22 bounded AFK Flow test reservation expired (catalog side): with NO_PAYMENT the timer releases C-11 and publishes StockReleased RESERVATION_EXPIRED; with STARTED it releases nothing; C-02 is not touched before its deadline
      model: ../arcadia-editions-architecture/business-flows/place-order-flow.zfl:268-297, ../arcadia-editions-architecture/business-flows/place-order-flow.zfl:340-344
      test data: data.sql (C-02, C-06, C-11)
      tests: PlaceOrderFlowReservationExpiredTest
      blocked by: T-21

## S5 late authorization rejected
path: ... > PaymentAuthorized (after expiry) > confirmOrder > confirmStockReservation[StockReservationNotHeldResult]
      > OrderConfirmationRejected > voidPayment > PaymentVoided > releaseStock (no-op) > end orderCancelled
review: [ ]

- [ ] T-23 bounded AFK confirmStockReservation NOT_HELD: when some line of the order is not RESERVED (released by the timer) answer NOT_HELD, no change, no event; CONFIRMED stays the redelivery answer
      model: ../arcadia-editions-architecture/business-flows/place-order-flow.zfl:166-176, ../catalog-inventory-api/domain-model.zdl:109-129
      test data: data.sql (C-05 not held, C-01 redelivery)
      tests: InventoryServiceTest#confirmStockReservationNotHeldTest
      blocked by: T-10
- [ ] T-24 bounded AFK Flow test late authorization (catalog side): POST confirm for C-05 answers NOT_HELD and publishes nothing; the release that follows (PaymentVoidFailed for C-05) is a no-op
      model: ../arcadia-editions-architecture/business-flows/place-order-flow.zfl:147-176, ../arcadia-editions-architecture/business-flows/place-order-flow.zfl:262-266
      test data: data.sql (C-05, C-06)
      tests: PlaceOrderFlowLateAuthorizationTest
      blocked by: T-15, T-23

## S6 backoffice (outside the flow)
review: [ ]

- [ ] T-25 bounded HITL Decide whether adjustStock may take availableQuantity below 0, and the answers for an unknown sku, a duplicate sku in createInventoryPosition and an orderId with no reservations; the models do not say
      model: ../catalog-inventory-api/domain-model.zdl:243-248, ../catalog-inventory-api/domain-model.zdl:259-269
- [ ] T-26 bounded AFK createInventoryPosition: availableQuantity = initialQuantity, reserved and sold 0; duplicate sku answered per T-25
      model: ../catalog-inventory-api/domain-model.zdl:224-234, ../catalog-inventory-api/domain-model.zdl:261-262
      test data: data.sql (SKU-001 duplicate; a new sku such as SKU-099)
      tests: InventoryBackofficeServiceTest#createInventoryPositionTest, InventoryBackofficeApiControllerTest#createInventoryPositionTest, InventoryBackofficeApiIntegrationTest#testCreateInventoryPosition_201
      blocked by: T-01, T-04, T-25
- [ ] T-27 bounded AFK receiveStock: load by path sku, availableQuantity += quantity, persist, emit StockReceived with the counters, reason, reference and receivedAt from the Clock
      model: ../catalog-inventory-api/domain-model.zdl:236-241, ../catalog-inventory-api/domain-model.zdl:264-265, ../catalog-inventory-api/domain-model.zdl:271-281
      test data: data.sql (SKU-001)
      tests: InventoryBackofficeServiceTest#receiveStockTest, InventoryBackofficeApiControllerTest#receiveStockTest, InventoryBackofficeApiIntegrationTest#testReceiveStock_201
      blocked by: T-01, T-03, T-04, T-08, T-25
- [ ] T-28 bounded AFK adjustStock: load by path sku, availableQuantity += quantityDelta (limit per T-25), persist, emit StockAdjusted with the counters, reason, reference and adjustedAt from the Clock
      model: ../catalog-inventory-api/domain-model.zdl:243-256, ../catalog-inventory-api/domain-model.zdl:267-268, ../catalog-inventory-api/domain-model.zdl:283-293
      test data: data.sql (SKU-001)
      tests: InventoryBackofficeServiceTest#adjustStockTest, InventoryBackofficeApiControllerTest#adjustStockTest, InventoryBackofficeApiIntegrationTest#testAdjustStock_201
      blocked by: T-01, T-03, T-04, T-08, T-25
