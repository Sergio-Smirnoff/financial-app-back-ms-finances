package com.financialapp.finances.infrastructure.messaging.mapper;

import com.financialapp.finances.domain.event.TransactionCreated;
import com.financialapp.finances.domain.event.TransactionReversed;
import com.financialapp.finances.infrastructure.messaging.payload.TransactionCreatedEvent;
import org.springframework.stereotype.Component;

@Component
public class TransactionEventMapper {

    /** Render a balance movement to the wire shape. {@code outboxId} fills the idempotency field. */
    public TransactionCreatedEvent transactionToWire(TransactionCreated e, Long outboxId) {
        return new TransactionCreatedEvent(
                outboxId,
                e.accountCbu().cbuNumber(),
                e.signedAmount(),
                e.currency().getCurrencyCode());
    }

    /** Render a reversal's compensating balance adjustment to the same wire shape (amount already negated). */
    public TransactionCreatedEvent reversalTransactionToWire(TransactionReversed e, Long outboxId) {
        return new TransactionCreatedEvent(
                outboxId,
                e.accountCbu().cbuNumber(),
                e.signedAmount(),
                e.currency().getCurrencyCode());
    }
}
