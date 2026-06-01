package com.financialapp.finances.infrastructure.messaging.payload;

import java.math.BigDecimal;

/**
 * Wire shape ms-banks consumes on topic {@code transaction.created}. {@code transactionId} carries
 * the OUTBOX ROW ID (unique per balance movement) — ms-banks dedups on it. {@code amount} is signed
 * (− debit / + credit). Field names are fixed by ms-banks and must not change.
 */
public record TransactionCreatedEvent(
        Long transactionId,
        String accountCbu,
        BigDecimal amount,
        String currency) {}
