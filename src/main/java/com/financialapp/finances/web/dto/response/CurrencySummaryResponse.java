package com.financialapp.finances.web.dto.response;

import java.math.BigDecimal;

/**
 * Per-currency totals in the summary response. The currency is the key of the enclosing map, so it
 * is not repeated here.
 */
public record CurrencySummaryResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance) {}
