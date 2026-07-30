package com.financialapp.finances.application.rule.impl;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.rule.CategorizationRule;
import com.financialapp.finances.domain.repository.CategorizationRuleRepository;
import com.financialapp.finances.domain.service.TransactionAutoCategorizer;
import com.financialapp.finances.domain.usecase.rule.CategorySuggestion;
import com.financialapp.finances.domain.usecase.rule.SuggestCategories;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SuggestCategoriesUseCaseImpl implements SuggestCategories {

    private final CategorizationRuleRepository ruleRepository;
    private final TransactionAutoCategorizer autoCategorizer = new TransactionAutoCategorizer();

    @Override
    @Transactional(readOnly = true)
    public List<CategorySuggestion> execute(UserId userId, List<String> descriptions) {
        if (descriptions == null || descriptions.isEmpty()) {
            return List.of();
        }
        List<CategorizationRule> rules = ruleRepository.findByUser(userId);

        return descriptions.stream()
                .map(desc -> {
                    Optional<CategoryId> suggested = autoCategorizer.suggest(desc, rules);
                    return new CategorySuggestion(desc, suggested.orElse(null));
                })
                .toList();
    }
}
