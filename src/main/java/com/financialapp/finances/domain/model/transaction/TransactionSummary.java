package com.financialapp.finances.domain.model.transaction;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Income/expense/balance totals for a single currency. Totals are never summed across currencies
 * (that would require FX conversion); a user with movements in several currencies has one
 * {@code TransactionSummary} per currency. {@code balance} is always {@code totalIncome - totalExpense}.
 */
public record TransactionSummary(
        Currency currency,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance) {}
