package com.financialapp.finances.domain.exception;

import com.financialapp.finances.domain.common.model.SupportedCurrency;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class InvalidCurrencyException extends DomainException {
    public InvalidCurrencyException(String value) {
        super(DomainErrorCode.INVALID_CURRENCY,
              "Unsupported currency code: '" + value + "'. Expected one of the supported ISO 4217 "
                  + "codes (" + Arrays.stream(SupportedCurrency.values())
                      .map(Enum::name).collect(Collectors.joining(", ")) + ").",
              Map.of("value", String.valueOf(value)));
    }
}
