package com.financialapp.finances.domain.model.budget;

import com.financialapp.finances.domain.common.model.DateRange;

import java.time.LocalDate;
import java.time.YearMonth;

public record BudgetPeriod(int year, int month) {

    public BudgetPeriod {
        if (year <= 0) {
            throw new IllegalArgumentException("year must be positive");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month must be between 1 and 12");
        }
    }

    public static BudgetPeriod from(LocalDate date) {
        return new BudgetPeriod(date.getYear(), date.getMonthValue());
    }

    public DateRange dateRange() {
        YearMonth yearMonth = YearMonth.of(year, month);
        return new DateRange(yearMonth.atDay(1), yearMonth.atEndOfMonth());
    }

    public BudgetPeriod previous() {
        YearMonth previous = YearMonth.of(year, month).minusMonths(1);
        return new BudgetPeriod(previous.getYear(), previous.getMonthValue());
    }

    public BudgetPeriod next() {
        YearMonth next = YearMonth.of(year, month).plusMonths(1);
        return new BudgetPeriod(next.getYear(), next.getMonthValue());
    }
}
