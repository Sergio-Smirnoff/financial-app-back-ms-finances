package com.financialapp.finances.domain.model.rule;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.RuleId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.exception.rule.InvalidCategorizationRuleException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CategorizationRuleTest {

    private static final UserId USER_ID = new UserId(42L);
    private static final CategoryId CATEGORY_ID = new CategoryId(10L);

    @Test
    void createsValidRule() {
        CategorizationRule rule = CategorizationRule.create(USER_ID, RuleMatchType.CONTAINS, "YPF", CATEGORY_ID);

        assertThat(rule.id()).isNull();
        assertThat(rule.userId()).isEqualTo(USER_ID);
        assertThat(rule.matchType()).isEqualTo(RuleMatchType.CONTAINS);
        assertThat(rule.pattern()).isEqualTo("YPF");
        assertThat(rule.categoryId()).isEqualTo(CATEGORY_ID);
        assertThat(rule.matchCount()).isZero();
        assertThat(rule.createdAt()).isNotNull();
    }

    @Test
    void rejectsBlankOrTooLongPattern() {
        assertThatThrownBy(() -> CategorizationRule.create(USER_ID, RuleMatchType.CONTAINS, "", CATEGORY_ID))
                .isInstanceOf(InvalidCategorizationRuleException.class);
        assertThatThrownBy(() -> CategorizationRule.create(USER_ID, RuleMatchType.CONTAINS, "   ", CATEGORY_ID))
                .isInstanceOf(InvalidCategorizationRuleException.class);
        String longPattern = "a".repeat(201);
        assertThatThrownBy(() -> CategorizationRule.create(USER_ID, RuleMatchType.CONTAINS, longPattern, CATEGORY_ID))
                .isInstanceOf(InvalidCategorizationRuleException.class);
    }

    @Test
    void rejectsNullRequiredFields() {
        assertThatThrownBy(() -> CategorizationRule.create(null, RuleMatchType.CONTAINS, "YPF", CATEGORY_ID))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> CategorizationRule.create(USER_ID, null, "YPF", CATEGORY_ID))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> CategorizationRule.create(USER_ID, RuleMatchType.CONTAINS, null, CATEGORY_ID))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> CategorizationRule.create(USER_ID, RuleMatchType.CONTAINS, "YPF", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void matchesContainsCaseInsensitive() {
        CategorizationRule rule = CategorizationRule.create(USER_ID, RuleMatchType.CONTAINS, "ypf", CATEGORY_ID);

        assertThat(rule.matches("YPF RUTA 9")).isTrue();
        assertThat(rule.matches("ypf estaciones")).isTrue();
        assertThat(rule.matches("UBER TRIP")).isFalse();
        assertThat(rule.matches(null)).isFalse();
        assertThat(rule.matches("")).isFalse();
    }

    @Test
    void matchesMerchantExactCaseInsensitiveAndTrimmed() {
        CategorizationRule rule = CategorizationRule.create(USER_ID, RuleMatchType.MERCHANT_EXACT, "UBER", CATEGORY_ID);

        assertThat(rule.matches("uber")).isTrue();
        assertThat(rule.matches("  UBER  ")).isTrue();
        assertThat(rule.matches("UBER TRIP")).isFalse();
    }

    @Test
    void recordMatchReturnsCopyIncrementingCount() {
        CategorizationRule rule = CategorizationRule.reconstitute(
                new RuleId(1L), USER_ID, RuleMatchType.CONTAINS, "YPF", CATEGORY_ID, 5, LocalDateTime.now());

        CategorizationRule updated = rule.recordMatch();

        assertThat(updated).isNotSameAs(rule);
        assertThat(rule.matchCount()).isEqualTo(5);
        assertThat(updated.matchCount()).isEqualTo(6);
        assertThat(updated.id()).isEqualTo(rule.id());
    }
}
