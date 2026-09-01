package com.financialapp.finances.web.dto.response;

public record BudgetResponse(
        Long categoryId,
        String categoryName,
        int year,
        int month,
        String amount,
        String currency,
        String alertThresholdPct
) { }
