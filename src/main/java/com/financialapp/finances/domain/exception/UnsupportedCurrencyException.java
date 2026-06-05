package com.financialapp.finances.domain.exception;

import com.financialapp.commons.core.error.DomainException;

import java.util.Currency;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Thrown by non-web entry points (Kafka listeners, internal commands) when a currency is valid ISO
 * but not in the runtime whitelist. Web requests are rejected earlier by {@code @SupportedCurrency}.
 */
public class UnsupportedCurrencyException extends DomainException {
    public UnsupportedCurrencyException(String code, Set<Currency> allowed) {
        super(DomainErrorCode.UNSUPPORTED_CURRENCY,
              "Currency " + code + " is not supported. Allowed: "
                  + allowed.stream().map(Currency::getCurrencyCode).sorted().collect(Collectors.toList()),
              Map.of("value", String.valueOf(code)));
    }
}
