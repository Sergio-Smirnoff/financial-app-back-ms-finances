package com.financialapp.finances.domain.common.model;

import com.financialapp.finances.domain.exception.CurrencyMismatchException;
import com.financialapp.finances.domain.exception.InvalidMoneyException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

/**
 * A positive monetary magnitude in a single {@link Currency}. {@code amount} is always
 * greater than zero — direction (expense / income / transfer) is not carried by Money; it is
 * derived per account from transaction ownership (see {@code Transaction.signedFor}, slice 3).
 * The amount is normalised to scale 2 (HALF_EVEN). The currency must be one the application
 * supports ({@link SupportedCurrency}); every invariant is enforced in the canonical
 * constructor so no construction path can bypass it.
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
        if (amount.signum() <= 0) {
            throw new InvalidMoneyException("amount must be positive (greater than zero)");
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

    private void requireSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new CurrencyMismatchException(
                currency.getCurrencyCode(), other.currency.getCurrencyCode());
        }
    }
}
