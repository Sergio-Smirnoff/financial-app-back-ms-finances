package com.financialapp.finances.domain.model.category;

import com.financialapp.finances.domain.exception.category.InvalidCategoryNameException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CategoryNameTest {

    @Test void trimsValue() {
        assertThat(new CategoryName("  Food  ").value()).isEqualTo("Food");
    }

    @Test void hasValueEquality() {
        assertThat(new CategoryName("Food")).isEqualTo(new CategoryName("Food"));
    }

    @Test void rejectsNull() {
        assertThatThrownBy(() -> new CategoryName(null))
            .isInstanceOf(InvalidCategoryNameException.class);
    }

    @Test void rejectsBlank() {
        assertThatThrownBy(() -> new CategoryName("   "))
            .isInstanceOf(InvalidCategoryNameException.class);
    }

    @Test void rejectsLongerThan100() {
        assertThatThrownBy(() -> new CategoryName("x".repeat(101)))
            .isInstanceOf(InvalidCategoryNameException.class);
    }
}
