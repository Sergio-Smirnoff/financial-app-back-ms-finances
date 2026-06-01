package com.financialapp.finances.domain.model.transaction;

import com.financialapp.finances.domain.common.model.Cbu;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BalanceMovementTest {

    private static final Currency ARS = Currency.getInstance("ARS");
    private static final Cbu ACCOUNT = new Cbu("0".repeat(21) + "1");

    @Test void holdsAccountSignedAmountAndCurrency() {
        BalanceMovement m = new BalanceMovement(ACCOUNT, new BigDecimal("-100.00"), ARS);
        assertThat(m.account()).isEqualTo(ACCOUNT);
        assertThat(m.signedAmount()).isEqualByComparingTo("-100.00");
        assertThat(m.currency()).isEqualTo(ARS);
    }

    @Test void rejectsZeroSignedAmount() {
        assertThatThrownBy(() -> new BalanceMovement(ACCOUNT, BigDecimal.ZERO, ARS))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test void rejectsNullSignedAmount() {
        assertThatThrownBy(() -> new BalanceMovement(ACCOUNT, null, ARS))
            .isInstanceOf(NullPointerException.class);
    }

    @Test void reversedNegatesAmountKeepingAccountAndCurrency() {
        BalanceMovement debit = new BalanceMovement(ACCOUNT, new BigDecimal("-100.00"), ARS);
        BalanceMovement reversed = debit.reversed();
        assertThat(reversed.account()).isEqualTo(debit.account());
        assertThat(reversed.currency()).isEqualTo(ARS);
        assertThat(reversed.signedAmount()).isEqualByComparingTo("100.00");
    }
}
