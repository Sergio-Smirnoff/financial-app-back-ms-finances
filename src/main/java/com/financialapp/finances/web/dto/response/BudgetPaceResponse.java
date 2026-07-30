package com.financialapp.finances.web.dto.response;

public record BudgetPaceResponse(
        Long categoryId,
        String categoryName,
        String spent,
        String remaining,
        String pctUsed,
        String expectedPctByToday,
        boolean overBudget,
        String currency
) { }
