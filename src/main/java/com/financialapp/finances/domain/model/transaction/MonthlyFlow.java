package com.financialapp.finances.domain.model.transaction;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Currency;

/**
 * Monthly income and expense totals for a specific currency in a calendar month.
 */
public record MonthlyFlow(
        YearMonth month,
        Currency currency,
        BigDecimal income,
        BigDecimal expense
) {
    public MonthlyFlow {
        if (month == null || currency == null) {
            throw new IllegalArgumentException("month and currency are required");
        }
        if (income == null) {
            income = BigDecimal.ZERO;
        }
        if (expense == null) {
            expense = BigDecimal.ZERO;
        }
    }
}
