package com.financialapp.finances.domain.service;

import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.Money;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.exception.transaction.UnownedTransactionException;
import com.financialapp.finances.domain.model.transaction.BalanceMovement;
import com.financialapp.finances.domain.model.transaction.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionPostingTest {

    private static final Currency ARS = Currency.getInstance("ARS");
    private static final UserId USER = new UserId(1L);
    private static final Cbu FROM = new Cbu("0".repeat(21) + "1");
    private static final Cbu TO = new Cbu("0".repeat(21) + "2");
    private static final CategoryId CATEGORY = new CategoryId(10L);
    private static final LocalDate DATE = LocalDate.of(2026, 5, 31);

    private final TransactionPosting posting = new TransactionPosting();

    private static Transaction tx() {
        return Transaction.create(USER, FROM, TO,
            new Money(new BigDecimal("100.00"), ARS), CATEGORY, "x", DATE);
    }

    @Test void expenseWhenOnlySourceOwned() {
        List<BalanceMovement> movements = posting.post(tx(), Set.of(FROM));
        assertThat(movements).singleElement().satisfies(m -> {
            assertThat(m.account()).isEqualTo(FROM);
            assertThat(m.signedAmount()).isEqualByComparingTo("-100.00");
            assertThat(m.currency()).isEqualTo(ARS);
        });
    }

    @Test void incomeWhenOnlyDestinationOwned() {
        List<BalanceMovement> movements = posting.post(tx(), Set.of(TO));
        assertThat(movements).singleElement().satisfies(m -> {
            assertThat(m.account()).isEqualTo(TO);
            assertThat(m.signedAmount()).isEqualByComparingTo("100.00");
        });
    }

    @Test void transferWhenBothOwned() {
        List<BalanceMovement> movements = posting.post(tx(), Set.of(FROM, TO));
        assertThat(movements).hasSize(2);
        assertThat(movements).anySatisfy(m -> {
            assertThat(m.account()).isEqualTo(FROM);
            assertThat(m.signedAmount()).isEqualByComparingTo("-100.00");
        });
        assertThat(movements).anySatisfy(m -> {
            assertThat(m.account()).isEqualTo(TO);
            assertThat(m.signedAmount()).isEqualByComparingTo("100.00");
        });
    }

    @Test void rejectsWhenNeitherOwned() {
        assertThatThrownBy(() -> posting.post(tx(), Set.of()))
            .isInstanceOf(UnownedTransactionException.class);
    }

    @Test void movementsAreImmutable() {
        List<BalanceMovement> movements = posting.post(tx(), Set.of(FROM));
        assertThatThrownBy(movements::clear)
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
