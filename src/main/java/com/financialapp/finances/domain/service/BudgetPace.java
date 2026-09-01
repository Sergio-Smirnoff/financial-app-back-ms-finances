package com.financialapp.finances.domain.service;

import com.financialapp.finances.domain.common.model.DateRange;
import com.financialapp.finances.domain.common.model.Money;
import com.financialapp.finances.domain.exception.budget.InvalidBudgetException;
import com.financialapp.finances.domain.model.budget.Budget;
import com.financialapp.finances.domain.model.budget.BudgetPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

public class BudgetPace {

    public BudgetPaceResult evaluate(Budget budget, Money actualSpend, LocalDate today) {
        Objects.requireNonNull(budget, "budget must not be null");
        Objects.requireNonNull(today, "today must not be null");

        BigDecimal budgetAmount = budget.amount().amount();
        BigDecimal spendAmount;

        if (actualSpend != null) {
            if (!budget.amount().currency().equals(actualSpend.currency())) {
                throw new InvalidBudgetException("Currency mismatch between budget ("
                        + budget.amount().currency() + ") and spend (" + actualSpend.currency() + ")");
            }
            spendAmount = actualSpend.amount();
        } else {
            spendAmount = BigDecimal.ZERO;
        }

        BigDecimal pctUsed = spendAmount.divide(budgetAmount, 4, RoundingMode.HALF_EVEN)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_EVEN);

        boolean overBudget = spendAmount.compareTo(budgetAmount) > 0;

        Money remaining = null;
        if (spendAmount.compareTo(budgetAmount) < 0) {
            remaining = actualSpend != null ? budget.amount().subtract(actualSpend) : budget.amount();
        }

        BudgetPeriod period = budget.period();
        DateRange dateRange = period.dateRange();

        BigDecimal expectedPctByToday;
        if (today.isBefore(dateRange.from())) {
            expectedPctByToday = new BigDecimal("0.00");
        } else if (today.isAfter(dateRange.to())) {
            expectedPctByToday = new BigDecimal("100.00");
        } else {
            YearMonth yearMonth = YearMonth.of(period.year(), period.month());
            int daysInMonth = yearMonth.lengthOfMonth();
            int dayOfMonth = today.getDayOfMonth();
            expectedPctByToday = BigDecimal.valueOf(dayOfMonth)
                    .divide(BigDecimal.valueOf(daysInMonth), 4, RoundingMode.HALF_EVEN)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_EVEN);
        }

        return new BudgetPaceResult(actualSpend, remaining, pctUsed, expectedPctByToday, overBudget);
    }
}
