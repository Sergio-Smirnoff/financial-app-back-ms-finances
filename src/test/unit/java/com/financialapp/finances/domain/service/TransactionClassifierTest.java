package com.financialapp.finances.domain.service;

import com.financialapp.finances.domain.common.model.*;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.model.transaction.TransactionKind;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionClassifierTest {

    private static final Currency ARS = Currency.getInstance("ARS");
    private final TransactionClassifier classifier = new TransactionClassifier();
    private final Cbu a = new Cbu("0001112223334445556667");
    private final Cbu b = new Cbu("9998887776665554443332");

    private Transaction tx() {
        return Transaction.create(new UserId(42L), a, b,
                new Money(new BigDecimal("100.00"), ARS), new CategoryId(5L), "x", LocalDate.of(2026, 6, 1));
    }

    @Test
    void ownsSourceOnlyIsExpense() {
        assertThat(classifier.classify(tx(), Set.of(a))).isEqualTo(TransactionKind.EXPENSE);
    }

    @Test
    void ownsDestinationOnlyIsIncome() {
        assertThat(classifier.classify(tx(), Set.of(b))).isEqualTo(TransactionKind.INCOME);
    }

    @Test
    void ownsBothIsTransfer() {
        assertThat(classifier.classify(tx(), Set.of(a, b))).isEqualTo(TransactionKind.TRANSFER);
    }

    @Test
    void ownsNeitherThrows() {
        assertThatThrownBy(() -> classifier.classify(tx(), Set.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
