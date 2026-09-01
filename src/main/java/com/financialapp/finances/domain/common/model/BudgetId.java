package com.financialapp.finances.domain.common.model;

public record BudgetId(Long value) {
    public BudgetId {
        IdentifiersValidator.requirePositiveIdentifier(value, "budgetId");
    }
}
