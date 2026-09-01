package com.financialapp.finances.domain.service;

import com.financialapp.finances.domain.common.model.Money;

import java.math.BigDecimal;

public record BudgetPaceResult(
        Money spent,
        Money remaining,
        BigDecimal pctUsed,
        BigDecimal expectedPctByToday,
        boolean overBudget
) { }
