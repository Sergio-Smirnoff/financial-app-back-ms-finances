package com.financialapp.finances.application.rule.impl;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.category.CategoryNames;
import com.financialapp.finances.domain.model.rule.CategorizationRule;
import com.financialapp.finances.domain.repository.CategorizationRuleRepository;
import com.financialapp.finances.domain.repository.CategoryRepository;
import com.financialapp.finances.domain.usecase.rule.ListCategorizationRules;
import com.financialapp.finances.domain.usecase.rule.RuleView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListCategorizationRulesUseCaseImpl implements ListCategorizationRules {

    private final CategorizationRuleRepository ruleRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RuleView> execute(UserId userId) {
        List<CategorizationRule> rules = ruleRepository.findByUser(userId);
        return rules.stream()
                .map(r -> new RuleView(r, resolveCategoryName(r.categoryId())))
                .toList();
    }

    private String resolveCategoryName(CategoryId categoryId) {
        CategoryNames names = categoryRepository.findNamesById(categoryId).orElse(new CategoryNames(null, null));
        return names.subcategory() != null ? names.subcategory() : names.category();
    }
}
