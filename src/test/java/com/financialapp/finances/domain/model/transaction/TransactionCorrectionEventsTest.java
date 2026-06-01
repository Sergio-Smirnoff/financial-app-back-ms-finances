package com.financialapp.finances.domain.model.transaction;

import com.financialapp.finances.domain.common.model.*;
import com.financialapp.finances.domain.event.DomainEvent;
import com.financialapp.finances.domain.event.TransactionCreated;
import com.financialapp.finances.domain.event.TransactionReversed;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionCorrectionEventsTest {

    private static final Currency ARS = Currency.getInstance("ARS");
    private final Cbu a = new Cbu("0001112223334445556667");
    private final Cbu b = new Cbu("9998887776665554443332");

    private Transaction persisted(BigDecimal amount) {
        return Transaction.reconstitute(new TransactionId(7L), new UserId(42L), a, b,
                new Money(amount, ARS), new CategoryId(5L), "x", LocalDate.of(2026, 6, 1));
    }

    @Test
    void recordReversalEmitsOneTransactionReversedPerMovementNegated() {
        Transaction tx = persisted(new BigDecimal("100.00"));
        BalanceMovement debitA = new BalanceMovement(a, new BigDecimal("-100.00"), ARS);

        tx.recordReversal(List.of(debitA));
        List<DomainEvent> events = tx.pullDomainEvents();

        assertThat(events).hasSize(1);
        TransactionReversed e = (TransactionReversed) events.get(0);
        assertThat(e.accountCbu()).isEqualTo(a);
        assertThat(e.signedAmount()).isEqualByComparingTo("100.00"); // negated
    }

    @Test
    void recordCorrectionEmitsReversedForOldThenCreatedForNew() {
        Transaction tx = persisted(new BigDecimal("150.00"));
        BalanceMovement oldDebit = new BalanceMovement(a, new BigDecimal("-100.00"), ARS);
        BalanceMovement newDebit = new BalanceMovement(a, new BigDecimal("-150.00"), ARS);

        tx.recordCorrection(List.of(oldDebit), List.of(newDebit));
        List<DomainEvent> events = tx.pullDomainEvents();

        assertThat(events).hasSize(2);
        assertThat(events.get(0)).isInstanceOf(TransactionReversed.class);
        assertThat(((TransactionReversed) events.get(0)).signedAmount()).isEqualByComparingTo("100.00");
        assertThat(events.get(1)).isInstanceOf(TransactionCreated.class);
        assertThat(((TransactionCreated) events.get(1)).signedAmount()).isEqualByComparingTo("-150.00");
    }

    @Test
    void requiresPersistedId() {
        Transaction unsaved = Transaction.create(new UserId(42L), a, b,
                new Money(new BigDecimal("100.00"), ARS), new CategoryId(5L), "x", LocalDate.of(2026, 6, 1));
        assertThatThrownBy(() -> unsaved.recordReversal(List.of(new BalanceMovement(a, new BigDecimal("-100.00"), ARS))))
                .isInstanceOf(IllegalStateException.class);
    }
}
