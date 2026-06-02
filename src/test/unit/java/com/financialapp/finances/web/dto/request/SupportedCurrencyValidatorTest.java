package com.financialapp.finances.web.dto.request;

import com.financialapp.finances.domain.gateway.SupportedCurrencies;
import org.junit.jupiter.api.Test;

import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SupportedCurrencyValidatorTest {

    private final SupportedCurrencies port = mock(SupportedCurrencies.class);
    private final SupportedCurrencyValidator validator = new SupportedCurrencyValidator(port);

    @Test
    void nullIsValid() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    void supportedCodeIsValid() {
        when(port.isSupported(Currency.getInstance("ARS"))).thenReturn(true);
        assertThat(validator.isValid("ARS", null)).isTrue();
    }

    @Test
    void unsupportedButValidIsoIsInvalid() {
        when(port.isSupported(Currency.getInstance("JPY"))).thenReturn(false);
        assertThat(validator.isValid("JPY", null)).isFalse();
    }

    @Test
    void nonIsoCodeIsInvalidWithoutLeakingException() {
        assertThat(validator.isValid("XYZ_NOT_ISO", null)).isFalse();
    }
}
