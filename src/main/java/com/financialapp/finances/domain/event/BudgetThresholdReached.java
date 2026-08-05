package com.financialapp.finances.domain.event;

import com.financialapp.finances.domain.common.model.BudgetId;
import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.budget.BudgetPeriod;

import java.math.BigDecimal;
import java.util.Objects;

public record BudgetThresholdReached(
        BudgetId budgetId,
        UserId userId,
        CategoryId categoryId,
        BigDecimal pctUsed,
        BigDecimal alertThresholdPct,
        BudgetPeriod period
) implements DomainEvent {
    public BudgetThresholdReached {
        Objects.requireNonNull(budgetId, "budgetId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(categoryId, "categoryId must not be null");
        Objects.requireNonNull(pctUsed, "pctUsed must not be null");
        Objects.requireNonNull(alertThresholdPct, "alertThresholdPct must not be null");
        Objects.requireNonNull(period, "period must not be null");
    }
}
