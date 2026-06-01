-- ═══════════════════════════════════════════════════════════════════
-- V16: Transactional outbox for balance events to ms-banks.
-- One row per balance movement; id (BIGSERIAL) is the wire idempotency id
-- (placed in the transaction.created "transactionId" field ms-banks dedups on).
-- ═══════════════════════════════════════════════════════════════════

CREATE TABLE finances.outbox_event
(
    id            BIGSERIAL    PRIMARY KEY,
    topic         VARCHAR(100) NOT NULL,
    aggregate_key VARCHAR(50)  NOT NULL,   -- Kafka partition key (sourceTransactionId)
    payload       JSONB        NOT NULL,   -- serialized wire TransactionCreatedEvent
    sent          BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    sent_at       TIMESTAMP    NULL
);

CREATE INDEX idx_outbox_event_unsent ON finances.outbox_event (id) WHERE sent = FALSE;
