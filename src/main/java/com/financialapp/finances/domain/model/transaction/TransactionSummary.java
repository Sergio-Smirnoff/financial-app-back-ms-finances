package com.financialapp.finances.domain.model.transaction;

import java.math.BigDecimal;

/** User finance summary (single currency; multi-currency totals are out of scope for now). */
public record TransactionSummary(
        String currency,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance) {}
