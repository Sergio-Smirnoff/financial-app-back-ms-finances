package com.financialapp.finances.domain.event;

import com.financialapp.commons.core.domain.model.Cbu;
import com.financialapp.finances.domain.common.model.TransactionId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionReversedTest {

    private static final Currency ARS = Currency.getInstance("ARS");

    @Test
    void isADomainEventCarryingTheNegatedMovement() {
        TransactionReversed e = new TransactionReversed(
                new TransactionId(7L), new Cbu("0001112223334445556667"),
                new BigDecimal("1250.00"), ARS);
        assertThat(e).isInstanceOf(DomainEvent.class);
        assertThat(e.signedAmount()).isEqualByComparingTo("1250.00");
        assertThat(e.accountCbu().cbuNumber()).isEqualTo("0001112223334445556667");
    }

    @Test
    void rejectsNullsAndZero() {
        Cbu cbu = new Cbu("0001112223334445556667");
        assertThatThrownBy(() -> new TransactionReversed(null, cbu, BigDecimal.ONE, ARS))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new TransactionReversed(new TransactionId(1L), cbu, BigDecimal.ZERO, ARS))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
