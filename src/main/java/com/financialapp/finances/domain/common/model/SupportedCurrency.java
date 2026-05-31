package com.financialapp.finances.domain.common.model;

import com.financialapp.finances.domain.exception.InvalidCurrencyException;

import java.util.Currency;

/**
 * The closed set of ISO 4217 currencies this application accepts. Acts as the whitelist guard
 * over {@link java.util.Currency}: any code outside this set is rejected at the boundary, even
 * if it is a valid ISO currency the JVM knows about.
 */
public enum SupportedCurrency {

    ARS,
    USD,
    EUR;

    /**
     * Parses and validates a currency code, returning the {@link Currency} only when it is both
     * a real ISO 4217 code and one this application supports.
     */
    public static Currency requireSupported(String currencyCode) {
        Currency currency;
        try {
            currency = Currency.getInstance(currencyCode.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new InvalidCurrencyException(currencyCode);
        }
        return requireSupported(currency);
    }

    /** Validates that an already-parsed {@link Currency} is one the application supports. */
    public static Currency requireSupported(Currency currency) {
        if (!isSupported(currency)) {
            throw new InvalidCurrencyException(currency.getCurrencyCode());
        }
        return currency;
    }

    private static boolean isSupported(Currency currency) {
        for (SupportedCurrency supported : values()) {
            if (supported.name().equals(currency.getCurrencyCode())) {
                return true;
            }
        }
        return false;
    }
}
