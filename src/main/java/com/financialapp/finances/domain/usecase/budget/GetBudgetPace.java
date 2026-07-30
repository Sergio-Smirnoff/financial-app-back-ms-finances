package com.financialapp.finances.domain.usecase.budget;

import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.budget.BudgetPeriod;

import java.time.LocalDate;
import java.util.List;

public interface GetBudgetPace {
    List<BudgetPaceView> execute(UserId userId, BudgetPeriod period, LocalDate today);
}
