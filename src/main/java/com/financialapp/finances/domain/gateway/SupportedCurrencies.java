package com.financialapp.finances.domain.gateway;

import java.util.Currency;
import java.util.Set;

/**
 * Outbound port: which currencies this service accepts on new writes (a runtime policy). Returns
 * {@link Currency}, not String — callers take {@code .getCurrencyCode()} only at the wire boundary.
 * The implementation caches a config-driven set (infrastructure); the domain never branches on code.
 */
public interface SupportedCurrencies {
    boolean isSupported(Currency currency);
    Set<Currency> all();
}
