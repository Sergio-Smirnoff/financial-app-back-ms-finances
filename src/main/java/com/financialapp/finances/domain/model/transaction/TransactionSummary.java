package com.financialapp.finances.domain.model.transaction;

import java.math.BigDecimal;

/** User finance summary (single currency; multi-currency totals are slice-5 scope). */
public record TransactionSummary(
        String currency,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance) {}
