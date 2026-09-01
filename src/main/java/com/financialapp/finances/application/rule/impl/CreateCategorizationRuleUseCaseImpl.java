package com.financialapp.finances.application.rule.impl;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.model.category.CategoryNames;
import com.financialapp.finances.domain.model.rule.CategorizationRule;
import com.financialapp.finances.domain.repository.CategorizationRuleRepository;
import com.financialapp.finances.domain.repository.CategoryRepository;
import com.financialapp.finances.domain.usecase.rule.CreateCategorizationRule;
import com.financialapp.finances.domain.usecase.rule.RuleView;
import com.financialapp.finances.domain.usecase.rule.command.CreateCategorizationRuleCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateCategorizationRuleUseCaseImpl implements CreateCategorizationRule {

    private final CategorizationRuleRepository ruleRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public RuleView execute(CreateCategorizationRuleCommand command) {
        CategorizationRule rule = CategorizationRule.create(
                command.userId(),
                command.matchType(),
                command.pattern(),
                command.categoryId()
        );
        CategorizationRule saved = ruleRepository.save(rule);
        String categoryName = resolveCategoryName(saved.categoryId());
        return new RuleView(saved, categoryName);
    }

    private String resolveCategoryName(CategoryId categoryId) {
        CategoryNames names = categoryRepository.findNamesById(categoryId).orElse(new CategoryNames(null, null));
        return names.subcategory() != null ? names.subcategory() : names.category();
    }
}
