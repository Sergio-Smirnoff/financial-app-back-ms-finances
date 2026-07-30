package com.financialapp.finances.domain.service;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.model.rule.CategorizationRule;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class TransactionAutoCategorizer {

    public Optional<CategoryId> suggest(String description, List<CategorizationRule> rules) {
        if (description == null || description.isBlank() || rules == null || rules.isEmpty()) {
            return Optional.empty();
        }

        Comparator<CategorizationRule> comparator = Comparator
                .comparingInt(CategorizationRule::matchCount).reversed()
                .thenComparing(CategorizationRule::createdAt);

        return rules.stream()
                .filter(rule -> rule.matches(description))
                .sorted(comparator)
                .map(CategorizationRule::categoryId)
                .findFirst();
    }
}
