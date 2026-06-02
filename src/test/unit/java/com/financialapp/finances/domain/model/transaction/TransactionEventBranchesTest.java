package com.financialapp.finances.domain.model.transaction;

import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.Money;
import com.financialapp.finances.domain.common.model.UserId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionEventBranchesTest {

    private static final Currency ARS = Currency.getInstance("ARS");
    private static final Cbu FROM = new Cbu("0".repeat(21) + "1");
    private static final Cbu TO = new Cbu("0".repeat(21) + "2");

    private Transaction unpersisted() {
        return Transaction.create(new UserId(1L), FROM, TO,
                new Money(new BigDecimal("100.00"), ARS), new CategoryId(10L), "x", LocalDate.of(2026, 5, 31));
    }

    private BalanceMovement movement() {
        return new BalanceMovement(FROM, new BigDecimal("-100.00"), ARS);
    }

    @Test void recordCreationEvents_throws_whenNotPersisted() {
        // Given an unpersisted (null-id) transaction / When recording creation events / Then rejected
        assertThatThrownBy(() -> unpersisted().recordCreationEvents(List.of(movement())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test void recordReversal_throws_whenNotPersisted() {
        // Given an unpersisted transaction / When recording a reversal / Then rejected
        assertThatThrownBy(() -> unpersisted().recordReversal(List.of(movement())))
                .isInstanceOf(IllegalStateException.class);
    }
}
