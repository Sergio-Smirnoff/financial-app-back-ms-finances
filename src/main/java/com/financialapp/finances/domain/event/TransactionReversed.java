package com.financialapp.finances.domain.event;

import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.common.model.TransactionId;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

/**
 * Undo of a balance movement a transaction previously caused on an owned account (edit or delete).
 * {@code signedAmount} is the adjustment to send to ms-banks — i.e. the original movement negated.
 * Maps to the same wire {@code transaction.created} as {@link TransactionCreated}; ms-banks just
 * applies another signed balance delta (deduped on a fresh outbox id).
 */
public record TransactionReversed(
        TransactionId sourceTransactionId,
        Cbu accountCbu,
        BigDecimal signedAmount,
        Currency currency) implements DomainEvent {

    public TransactionReversed {
        Objects.requireNonNull(sourceTransactionId, "sourceTransactionId");
        Objects.requireNonNull(accountCbu, "accountCbu");
        Objects.requireNonNull(signedAmount, "signedAmount");
        Objects.requireNonNull(currency, "currency");
        if (signedAmount.signum() == 0) {
            throw new IllegalArgumentException("signedAmount must not be zero");
        }
    }
}
