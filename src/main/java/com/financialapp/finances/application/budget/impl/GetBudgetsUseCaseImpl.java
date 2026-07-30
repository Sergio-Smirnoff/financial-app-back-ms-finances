package com.financialapp.finances.application.budget.impl;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.budget.Budget;
import com.financialapp.finances.domain.model.budget.BudgetPeriod;
import com.financialapp.finances.domain.model.category.CategoryNames;
import com.financialapp.finances.domain.repository.BudgetRepository;
import com.financialapp.finances.domain.repository.CategoryRepository;
import com.financialapp.finances.domain.usecase.budget.BudgetView;
import com.financialapp.finances.domain.usecase.budget.GetBudgets;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetBudgetsUseCaseImpl implements GetBudgets {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BudgetView> execute(UserId userId, BudgetPeriod period) {
        List<Budget> budgets = budgetRepository.findByUserAndPeriod(userId, period);
        return budgets.stream()
                .map(b -> new BudgetView(b, resolveCategoryName(b.categoryId())))
                .toList();
    }

    private String resolveCategoryName(CategoryId categoryId) {
        CategoryNames names = categoryRepository.findNamesById(categoryId).orElse(new CategoryNames(null, null));
        return names.subcategory() != null ? names.subcategory() : names.category();
    }
}
