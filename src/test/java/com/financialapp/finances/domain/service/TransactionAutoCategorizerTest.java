package com.financialapp.finances.domain.service;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.RuleId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.rule.CategorizationRule;
import com.financialapp.finances.domain.model.rule.RuleMatchType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionAutoCategorizerTest {

    private static final UserId USER_ID = new UserId(42L);
    private static final CategoryId CAT_FUEL = new CategoryId(10L);
    private static final CategoryId CAT_TAXI = new CategoryId(20L);

    private final TransactionAutoCategorizer categorizer = new TransactionAutoCategorizer();

    @Test
    void suggestsCategoryFromFirstMatchingRule() {
        CategorizationRule rule1 = CategorizationRule.create(USER_ID, RuleMatchType.CONTAINS, "YPF", CAT_FUEL);
        CategorizationRule rule2 = CategorizationRule.create(USER_ID, RuleMatchType.CONTAINS, "UBER", CAT_TAXI);

        Optional<CategoryId> result = categorizer.suggest("YPF RUTA 9", List.of(rule1, rule2));

        assertThat(result).contains(CAT_FUEL);
    }

    @Test
    void ordersRulesByMatchCountDescThenCreatedAtAsc() {
        LocalDateTime now = LocalDateTime.now();
        // Rule with lower match count but earlier created
        CategorizationRule ruleLowMatch = CategorizationRule.reconstitute(
                new RuleId(1L), USER_ID, RuleMatchType.CONTAINS, "TRIP", CAT_FUEL, 2, now.minusDays(5));
        // Rule with higher match count
        CategorizationRule ruleHighMatch = CategorizationRule.reconstitute(
                new RuleId(2L), USER_ID, RuleMatchType.CONTAINS, "TRIP", CAT_TAXI, 10, now.minusDays(1));

        Optional<CategoryId> result = categorizer.suggest("UBER TRIP", List.of(ruleLowMatch, ruleHighMatch));

        assertThat(result).contains(CAT_TAXI);
    }

    @Test
    void returnsEmptyWhenNoRuleMatchesOrBlankDescription() {
        CategorizationRule rule = CategorizationRule.create(USER_ID, RuleMatchType.CONTAINS, "YPF", CAT_FUEL);

        assertThat(categorizer.suggest("SUPERMARKET", List.of(rule))).isEmpty();
        assertThat(categorizer.suggest("", List.of(rule))).isEmpty();
        assertThat(categorizer.suggest(null, List.of(rule))).isEmpty();
        assertThat(categorizer.suggest("YPF", List.of())).isEmpty();
    }
}
