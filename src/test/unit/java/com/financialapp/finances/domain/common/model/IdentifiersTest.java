package com.financialapp.finances.domain.common.model;

import com.financialapp.finances.domain.exception.InvalidIdentifierException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdentifiersTest {

    @Test void categoryIdAcceptsPositive() {
        assertThat(new CategoryId(5L).value()).isEqualTo(5L);
    }

    @Test void userIdAcceptsPositive() {
        assertThat(new UserId(7L).value()).isEqualTo(7L);
    }

    @Test void categoryIdRejectsNull() {
        assertThatThrownBy(() -> new CategoryId(null))
            .isInstanceOf(InvalidIdentifierException.class);
    }

    @Test void categoryIdRejectsNonPositive() {
        assertThatThrownBy(() -> new CategoryId(0L))
            .isInstanceOf(InvalidIdentifierException.class);
        assertThatThrownBy(() -> new CategoryId(-1L))
            .isInstanceOf(InvalidIdentifierException.class);
    }

    @Test void userIdRejectsNonPositive() {
        assertThatThrownBy(() -> new UserId(0L))
            .isInstanceOf(InvalidIdentifierException.class);
    }
}
