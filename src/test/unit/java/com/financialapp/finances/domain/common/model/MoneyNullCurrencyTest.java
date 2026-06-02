package com.financialapp.finances.domain.common.model;

import com.financialapp.finances.domain.exception.InvalidMoneyException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyNullCurrencyTest {

    @Test void rejects_whenCurrencyNull() {
        // Given a non-null amount but null currency / When constructed / Then rejected
        assertThatThrownBy(() -> new Money(new BigDecimal("10.00"), null))
                .isInstanceOf(InvalidMoneyException.class)
                .hasMessageContaining("currency");
    }
}
