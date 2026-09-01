package com.financialapp.finances.domain.usecase.budget;

import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.budget.BudgetPeriod;

import java.util.List;

public interface GetBudgets {
    List<BudgetView> execute(UserId userId, BudgetPeriod period);
}
