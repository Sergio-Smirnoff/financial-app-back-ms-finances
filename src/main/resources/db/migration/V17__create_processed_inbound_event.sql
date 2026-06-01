-- ═══════════════════════════════════════════════════════════════════
-- V17: Inbound dedup for payment-events (ms-banks → finances ledger).
-- payment-events carries no native id, so the key is a deterministic hash
-- of the event fields (see PaymentEventListener). Prevents double-recording
-- on Kafka redelivery. LIMITATION: two identical legitimate payments in one
-- day collide; acceptable v1, raised as a contract gap (design §7).
-- ═══════════════════════════════════════════════════════════════════

CREATE TABLE finances.processed_inbound_event
(
    dedup_key   VARCHAR(128) PRIMARY KEY,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);
