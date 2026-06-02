package com.financialapp.finances.domain.common.model;

import java.util.Currency;
import java.util.Objects;

/** An account the user owns, as known by ms-banks: its CBU paired with its authoritative currency. */
public record OwnedAccount(Cbu cbu, Currency currency) {

    public OwnedAccount {
        Objects.requireNonNull(cbu, "cbu must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
    }
}
