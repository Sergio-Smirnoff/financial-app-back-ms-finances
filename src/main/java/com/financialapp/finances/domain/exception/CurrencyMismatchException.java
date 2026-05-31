package com.financialapp.finances.domain.exception;

import java.util.Map;

public class CurrencyMismatchException extends DomainException {
    public CurrencyMismatchException(String leftCurrency, String rightCurrency) {
        super(DomainErrorCode.CURRENCY_MISMATCH,
              "Cannot operate on money of different currencies: " + leftCurrency + " vs " + rightCurrency + ".",
              Map.of("leftCurrency", leftCurrency, "rightCurrency", rightCurrency));
    }
}
