package com.financialapp.finances.domain.model.budget;

import com.financialapp.finances.domain.common.model.BudgetId;
import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.Money;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.exception.budget.InvalidBudgetException;

import java.math.BigDecimal;
import java.util.Objects;

public final class Budget {

    private final BudgetId id;
    private final UserId userId;
    private final CategoryId categoryId;
    private final BudgetPeriod period;
    private final Money amount;
    private final BigDecimal alertThresholdPct;
    private final BudgetPeriod lastAlertedPeriod;

    private Budget(BudgetId id, UserId userId, CategoryId categoryId, BudgetPeriod period,
                   Money amount, BigDecimal alertThresholdPct, BudgetPeriod lastAlertedPeriod) {
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.categoryId = Objects.requireNonNull(categoryId, "categoryId must not be null");
        this.period = Objects.requireNonNull(period, "period must not be null");
        this.amount = Objects.requireNonNull(amount, "amount must not be null");
        if (alertThresholdPct != null) {
            if (alertThresholdPct.compareTo(BigDecimal.ZERO) <= 0
                    || alertThresholdPct.compareTo(new BigDecimal("100")) > 0) {
                throw new InvalidBudgetException(
                    "alertThresholdPct must be between 0 (exclusive) and 100 (inclusive)");
            }
        }
        this.id = id;
        this.alertThresholdPct = alertThresholdPct;
        this.lastAlertedPeriod = lastAlertedPeriod;
    }

    public static Budget create(UserId userId, CategoryId categoryId, BudgetPeriod period,
                                Money amount, BigDecimal alertThresholdPct) {
        return new Budget(null, userId, categoryId, period, amount, alertThresholdPct, null);
    }

    public static Budget reconstitute(BudgetId id, UserId userId, CategoryId categoryId,
                                      BudgetPeriod period, Money amount, BigDecimal alertThresholdPct,
                                      BudgetPeriod lastAlertedPeriod) {
        return new Budget(Objects.requireNonNull(id, "id must not be null"),
                userId, categoryId, period, amount, alertThresholdPct, lastAlertedPeriod);
    }

    public Budget markAlerted(BudgetPeriod current) {
        Objects.requireNonNull(current, "current period must not be null");
        return new Budget(id, userId, categoryId, period, amount, alertThresholdPct, current);
    }

    public BudgetId id() { return id; }
    public UserId userId() { return userId; }
    public CategoryId categoryId() { return categoryId; }
    public BudgetPeriod period() { return period; }
    public Money amount() { return amount; }
    public BigDecimal alertThresholdPct() { return alertThresholdPct; }
    public BudgetPeriod lastAlertedPeriod() { return lastAlertedPeriod; }
}
