package com.financialapp.finances.domain.common.model;

import com.financialapp.finances.domain.exception.CurrencyMismatchException;
import com.financialapp.finances.domain.exception.InvalidMoneyException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    private static final Currency ARS = Currency.getInstance("ARS");
    private static final Currency USD = Currency.getInstance("USD");

    private static Money ars(String amount) {
        return new Money(new BigDecimal(amount), ARS);
    }

    @Test void normalisesScaleToTwo() {
        assertThat(ars("10").amount()).isEqualByComparingTo("10.00");
        assertThat(ars("10.005").amount()).isEqualByComparingTo("10.00"); // HALF_EVEN
    }

    @Test void addsAndSubtractsSameCurrency() {
        assertThat(ars("10.00").add(ars("5.50"))).isEqualTo(ars("15.50"));
        assertThat(ars("10.00").subtract(ars("4.00"))).isEqualTo(ars("6.00"));
    }

    @Test void rejectsZeroAmount() {
        assertThatThrownBy(() -> ars("0"))
            .isInstanceOf(InvalidMoneyException.class);
    }

    @Test void rejectsNegativeAmount() {
        assertThatThrownBy(() -> ars("-10.00"))
            .isInstanceOf(InvalidMoneyException.class);
    }

    @Test void subtractToNonPositiveIsRejected() {
        assertThatThrownBy(() -> ars("4.00").subtract(ars("4.00")))
            .isInstanceOf(InvalidMoneyException.class); // nets to zero → not a valid Money
        assertThatThrownBy(() -> ars("4.00").subtract(ars("9.00")))
            .isInstanceOf(InvalidMoneyException.class); // nets negative → not a valid Money
    }

    @Test void rejectsNullAmount() {
        assertThatThrownBy(() -> new Money(null, ARS))
            .isInstanceOf(InvalidMoneyException.class);
    }

    @Test void rejectsArithmeticAcrossCurrencies() {
        assertThatThrownBy(() -> ars("1.00").add(new Money(new BigDecimal("1.00"), USD)))
            .isInstanceOf(CurrencyMismatchException.class);
    }
}
