package com.financialapp.finances.domain.common.model;

import com.financialapp.finances.domain.exception.CurrencyMismatchException;
import com.financialapp.finances.domain.exception.InvalidMoneyException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

/**
 * A signed monetary amount in a single {@link Currency}. The sign carries direction:
 * positive = inflow/income, negative = outflow/expense — which is why no stored
 * TransactionType exists. The amount is normalised to scale 2 (HALF_EVEN) and may never be zero.
 * The currency must be one the application supports ({@link SupportedCurrency}); the invariant is
 * enforced in the constructor so no construction path can bypass it.
 */
public record Money(BigDecimal amount, Currency currency) {

    public Money {
        if (amount == null) {
            throw new InvalidMoneyException("amount must not be null");
        }
        if (currency == null) {
            throw new InvalidMoneyException("currency must not be null");
        }
        SupportedCurrency.requireSupported(currency);
        amount = amount.setScale(2, RoundingMode.HALF_EVEN);
        if (amount.signum() == 0) {
            throw new InvalidMoneyException("amount must be non-zero");
        }
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(amount.subtract(other.amount), currency);
    }

    public Money negate() {
        return new Money(amount.negate(), currency);
    }

    public boolean isInflow() {
        return amount.signum() > 0;
    }

    public boolean isOutflow() {
        return amount.signum() < 0;
    }

    private void requireSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new CurrencyMismatchException(
                currency.getCurrencyCode(), other.currency.getCurrencyCode());
        }
    }
}
