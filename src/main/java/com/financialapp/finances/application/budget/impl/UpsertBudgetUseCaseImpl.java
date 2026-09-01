package com.financialapp.finances.application.budget.impl;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.model.budget.Budget;
import com.financialapp.finances.domain.model.category.CategoryNames;
import com.financialapp.finances.domain.repository.BudgetRepository;
import com.financialapp.finances.domain.repository.CategoryRepository;
import com.financialapp.finances.domain.usecase.budget.BudgetView;
import com.financialapp.finances.domain.usecase.budget.UpsertBudget;
import com.financialapp.finances.domain.usecase.budget.command.UpsertBudgetCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpsertBudgetUseCaseImpl implements UpsertBudget {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public BudgetView execute(UpsertBudgetCommand command) {
        Budget budget = Budget.create(
                command.userId(),
                command.categoryId(),
                command.period(),
                command.amount(),
                command.alertThresholdPct()
        );
        Budget saved = budgetRepository.upsert(budget);
        String categoryName = resolveCategoryName(saved.categoryId());
        return new BudgetView(saved, categoryName);
    }

    private String resolveCategoryName(CategoryId categoryId) {
        CategoryNames names = categoryRepository.findNamesById(categoryId).orElse(new CategoryNames(null, null));
        return names.subcategory() != null ? names.subcategory() : names.category();
    }
}
