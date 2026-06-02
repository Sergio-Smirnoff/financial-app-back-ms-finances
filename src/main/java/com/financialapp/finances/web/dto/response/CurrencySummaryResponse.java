package com.financialapp.finances.web.dto.response;

/**
 * Per-currency totals in the summary response. The currency is the key of the enclosing map, so it
 * is not repeated here. Money is a decimal String (no BigDecimal on the wire).
 */
public record CurrencySummaryResponse(
        String totalIncome,
        String totalExpense,
        String balance) {}
