package com.financialapp.finances.infrastructure.config;

import org.junit.jupiter.api.Test;

import java.util.Currency;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SupportedCurrenciesImplInitTest {

    @Test
    void cachesConfiguredCurrencies() {
        SupportedCurrenciesImpl impl =
                new SupportedCurrenciesImpl(new CurrenciesProperties(Set.of("ARS", "USD")));
        impl.init();
        assertThat(impl.isSupported(Currency.getInstance("ARS"))).isTrue();
        assertThat(impl.isSupported(Currency.getInstance("USD"))).isTrue();
        assertThat(impl.isSupported(Currency.getInstance("JPY"))).isFalse();
        assertThat(impl.all()).containsExactlyInAnyOrder(
                Currency.getInstance("ARS"), Currency.getInstance("USD"));
    }

    @Test
    void initRejectsACodeUnknownToTheJdk() {
        SupportedCurrenciesImpl impl =
                new SupportedCurrenciesImpl(new CurrenciesProperties(Set.of("XYZ")));
        assertThatThrownBy(impl::init).isInstanceOf(IllegalArgumentException.class);
    }
}
