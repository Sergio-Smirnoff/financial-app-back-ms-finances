package com.financialapp.finances.domain.exception;

import com.financialapp.commons.core.error.DomainException;

import java.util.Map;

public class CurrencyMismatchException extends DomainException {
    public CurrencyMismatchException(String leftCurrency, String rightCurrency) {
        super(DomainErrorCode.CURRENCY_MISMATCH,
              "Cannot operate on money of different currencies: " + leftCurrency + " vs " + rightCurrency + ".",
              Map.of("leftCurrency", leftCurrency, "rightCurrency", rightCurrency));
    }
}
