package com.financialapp.finances.domain.model.transaction;

import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.Money;
import com.financialapp.finances.domain.common.model.TransactionId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.exception.transaction.SameAccountTransactionException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionTest {

    private static final Currency ARS = Currency.getInstance("ARS");
    private static final UserId USER = new UserId(1L);
    private static final Cbu FROM = new Cbu("0".repeat(21) + "1");
    private static final Cbu TO = new Cbu("0".repeat(21) + "2");
    private static final CategoryId CATEGORY = new CategoryId(10L);
    private static final LocalDate DATE = LocalDate.of(2026, 5, 31);

    private static Money ars(String amount) {
        return new Money(new BigDecimal(amount), ARS);
    }

    private static Transaction tx() {
        return Transaction.create(USER, FROM, TO, ars("100.00"), CATEGORY, "lunch", DATE);
    }

    @Test void createSetsFieldsAndNullId() {
        Transaction t = tx();
        assertThat(t.id()).isNull();
        assertThat(t.userId()).isEqualTo(USER);
        assertThat(t.fromCbu()).isEqualTo(FROM);
        assertThat(t.toCbu()).isEqualTo(TO);
        assertThat(t.money()).isEqualTo(ars("100.00"));
        assertThat(t.categoryId()).isEqualTo(CATEGORY);
        assertThat(t.currency()).isEqualTo(ARS);
    }

    @Test void rejectsSameFromAndTo() {
        assertThatThrownBy(() -> Transaction.create(USER, FROM, FROM, ars("100.00"), CATEGORY, "x", DATE))
            .isInstanceOf(SameAccountTransactionException.class);
    }

    @Test void rejectsOverLongDescription() {
        String tooLong = "x".repeat(501);
        assertThatThrownBy(() -> Transaction.create(USER, FROM, TO, ars("1.00"), CATEGORY, tooLong, DATE))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test void allowsNullDescription() {
        assertThat(Transaction.create(USER, FROM, TO, ars("1.00"), CATEGORY, null, DATE).description())
            .isNull();
    }

    @Test void signedForCreditsDestinationAndDebitsSource() {
        Transaction t = tx();
        assertThat(t.signedFor(TO)).isEqualByComparingTo("100.00");
        assertThat(t.signedFor(FROM)).isEqualByComparingTo("-100.00");
    }

    @Test void signedForUnknownCbuThrows() {
        Cbu other = new Cbu("9".repeat(22));
        assertThatThrownBy(() -> tx().signedFor(other))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test void involvesOnlyItsTwoSides() {
        Transaction t = tx();
        assertThat(t.involves(FROM)).isTrue();
        assertThat(t.involves(TO)).isTrue();
        assertThat(t.involves(new Cbu("9".repeat(22)))).isFalse();
    }

    @Test void changeDetailsUpdatesEditableFieldsAndPreservesIdentity() {
        Transaction persisted = Transaction.reconstitute(
            new TransactionId(7L), USER, FROM, TO, ars("100.00"), CATEGORY, "old", DATE);
        Transaction updated = persisted.changeDetails(
            ars("250.00"), new CategoryId(20L), "new", DATE.plusDays(1));
        assertThat(updated.id()).isEqualTo(new TransactionId(7L));
        assertThat(updated.userId()).isEqualTo(USER);
        assertThat(updated.fromCbu()).isEqualTo(FROM);
        assertThat(updated.toCbu()).isEqualTo(TO);
        assertThat(updated.money()).isEqualTo(ars("250.00"));
        assertThat(updated.categoryId()).isEqualTo(new CategoryId(20L));
        assertThat(updated.description()).isEqualTo("new");
        assertThat(updated.date()).isEqualTo(DATE.plusDays(1));
    }
}
