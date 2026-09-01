package com.financialapp.finances.domain.usecase.budget;

import com.financialapp.finances.domain.usecase.budget.command.UpsertBudgetCommand;

public interface UpsertBudget {
    BudgetView execute(UpsertBudgetCommand command);
}
