package com.financialapp.finances.domain.exception;

import com.financialapp.commons.core.error.DomainException;
import org.junit.jupiter.api.Test;

import java.util.Currency;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UnsupportedCurrencyExceptionTest {

    @Test
    void carriesTheCodeTheErrorAndTheAllowedList() {
        UnsupportedCurrencyException ex = new UnsupportedCurrencyException(
                "JPY", Set.of(Currency.getInstance("ARS"), Currency.getInstance("USD")));

        assertThat(ex.getError()).isEqualTo(DomainErrorCode.UNSUPPORTED_CURRENCY);
        assertThat(ex.getMessage()).contains("JPY").contains("ARS").contains("USD");
        assertThat(ex).isInstanceOf(DomainException.class);
    }
}
