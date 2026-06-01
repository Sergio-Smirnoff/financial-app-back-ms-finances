package com.financialapp.finances.domain.common.model;

import com.financialapp.finances.domain.exception.InvalidIdentifierException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionIdTest {

    @Test void acceptsPositiveValue() {
        assertThat(new TransactionId(1L).value()).isEqualTo(1L);
    }

    @Test void rejectsNull() {
        assertThatThrownBy(() -> new TransactionId(null))
            .isInstanceOf(InvalidIdentifierException.class);
    }

    @Test void rejectsZeroOrNegative() {
        assertThatThrownBy(() -> new TransactionId(0L))
            .isInstanceOf(InvalidIdentifierException.class);
        assertThatThrownBy(() -> new TransactionId(-5L))
            .isInstanceOf(InvalidIdentifierException.class);
    }
}
