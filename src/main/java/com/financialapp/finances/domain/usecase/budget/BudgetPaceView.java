package com.financialapp.finances.domain.usecase.budget;

import com.financialapp.finances.domain.model.budget.Budget;
import com.financialapp.finances.domain.service.BudgetPaceResult;

public record BudgetPaceView(Budget budget, String categoryName, BudgetPaceResult pace) { }
