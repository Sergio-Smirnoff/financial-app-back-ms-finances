package com.financialapp.finances.domain.usecase.budget.command;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.Money;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.budget.BudgetPeriod;

import java.math.BigDecimal;

public record UpsertBudgetCommand(
        UserId userId,
        CategoryId categoryId,
        BudgetPeriod period,
        Money amount,
        BigDecimal alertThresholdPct
) { }
