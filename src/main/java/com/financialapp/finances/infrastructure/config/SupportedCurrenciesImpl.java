package com.financialapp.finances.infrastructure.config;

import com.financialapp.finances.domain.gateway.SupportedCurrencies;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Caches the configured whitelist once at boot. {@code Currency.getInstance} is the semantic check:
 * a syntactically valid but JDK-unknown code (e.g. XYZ) throws and stops startup.
 */
@Component
@RequiredArgsConstructor
public class SupportedCurrenciesImpl implements SupportedCurrencies {

    private final CurrenciesProperties properties;
    private Set<Currency> cached;

    @PostConstruct
    void init() {
        cached = properties.supported().stream()
                .map(Currency::getInstance)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean isSupported(Currency currency) {
        return cached.contains(currency);
    }

    @Override
    public Set<Currency> all() {
        return cached;
    }
}
