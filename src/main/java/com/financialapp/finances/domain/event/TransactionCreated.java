package com.financialapp.finances.domain.event;

import com.financialapp.commons.core.domain.model.Cbu;
import com.financialapp.finances.domain.common.model.TransactionId;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

/**
 * One signed balance movement a recorded transaction causes on an owned account, to be delivered
 * to ms-banks as a {@code transaction.created} balance event. Expense/income emit one; a transfer
 * emits two (one per owned side). {@code signedAmount} = {@code Transaction.signedFor(accountCbu)}.
 */
public record TransactionCreated(
        TransactionId sourceTransactionId,
        Cbu accountCbu,
        BigDecimal signedAmount,
        Currency currency) implements DomainEvent {

    public TransactionCreated {
        Objects.requireNonNull(sourceTransactionId, "sourceTransactionId");
        Objects.requireNonNull(accountCbu, "accountCbu");
        Objects.requireNonNull(signedAmount, "signedAmount");
        Objects.requireNonNull(currency, "currency");
        if (signedAmount.signum() == 0) {
            throw new IllegalArgumentException("signedAmount must not be zero");
        }
    }
}
