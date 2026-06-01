package com.financialapp.finances.domain.common.model;

/**
 * Identity of a Transaction aggregate. A scalar reference; never the Transaction object graph.
 * Constructed only for persisted transactions — the aggregate holds {@code null} until saved.
 */
public record TransactionId(Long value) {
    public TransactionId {
        IdentifiersValidator.requirePositiveIdentifier(value, "transactionId");
    }
}
