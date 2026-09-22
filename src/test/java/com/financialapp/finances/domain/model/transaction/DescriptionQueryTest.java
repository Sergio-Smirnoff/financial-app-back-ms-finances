package com.financialapp.finances.domain.model.transaction;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DescriptionQueryTest {

    @Test
    void trimsTheSuppliedText() {
        assertThat(new DescriptionQuery("  super  ").text()).isEqualTo("super");
    }

    @Test
    void rejectsBlankText() {
        assertThatThrownBy(() -> new DescriptionQuery("   ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DescriptionQuery(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void escapesLikeMetacharactersInTheContainsPattern() {
        assertThat(new DescriptionQuery("50%").containsPattern()).isEqualTo("%50\\%%");
        assertThat(new DescriptionQuery("a_b").containsPattern()).isEqualTo("%a\\_b%");
        assertThat(new DescriptionQuery("c\\d").containsPattern()).isEqualTo("%c\\\\d%");
        assertThat(new DescriptionQuery("super").containsPattern()).isEqualTo("%super%");
    }
}
