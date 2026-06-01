package com.financialapp.finances.domain.model.transaction;

import com.financialapp.finances.domain.common.model.Cbu;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

/**
 * One signed balance adjustment to a single owned account, derived by {@code TransactionPosting}.
 * Positive credits the account, negative debits it; never zero. Slice 4 turns each movement into
 * a balance-event on the wire to ms-banks.
 */
public record BalanceMovement(Cbu account, BigDecimal signedAmount, Currency currency) {
    public BalanceMovement {
        Objects.requireNonNull(account, "account must not be null");
        Objects.requireNonNull(signedAmount, "signedAmount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        if (signedAmount.signum() == 0) {
            throw new IllegalArgumentException("signedAmount must not be zero");
        }
    }
}
