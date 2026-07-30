package com.financialapp.finances.domain.usecase.budget;

import com.financialapp.finances.domain.model.budget.Budget;

public record BudgetView(Budget budget, String categoryName) { }
