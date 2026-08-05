package com.financialapp.finances.domain.repository;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.budget.Budget;
import com.financialapp.finances.domain.model.budget.BudgetPeriod;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository {
    Budget upsert(Budget budget);
    List<Budget> findByUserAndPeriod(UserId userId, BudgetPeriod period);
    List<Budget> findByPeriod(BudgetPeriod period);
    Optional<Budget> findByUserCategoryAndPeriod(UserId userId, CategoryId categoryId, BudgetPeriod period);
}
