# 0004. Avro on Kafka uses the Confluent serializer

Status: accepted, 2026-11-26 (demo: same wire format as orders-checkout ADR 0002 and payments-processing T-31)

## Context

orders-checkout reads StockReleased and payments-processing writes PaymentVoidFailed with the Confluent
`KafkaAvroSerializer` / `KafkaAvroDeserializer` (magic byte + schema id). The Spring Cloud Stream `application/*+avro`
converter (`spring-cloud-stream-schema-registry-client` in this pom) writes a framing they can't read.

## Decision

- Bindings use native encoding / decoding; `spring.kafka` values `KafkaAvroSerializer` / `KafkaAvroDeserializer`,
  `specific.avro.reader: true`, `schema.registry.url: ${SCHEMA_REGISTRY_URL:http://localhost:8081}`, default
  `TopicNameStrategy`.
- Keys are String (`StringSerializer` / `StringDeserializer`, replacing the archetype's Long); the generated
  producers send no key.
- `pom.xml`: `io.confluent:kafka-avro-serializer` 8.0.0 and the repository `https://packages.confluent.io/maven/`.

Board task T-08 does it with the bindings.

## Consequences

Wire compatible with the other Arcadia services. The outbox (ADR 0003) is unaffected: it stores the payload with
Jackson and the binder serializes it on send.
