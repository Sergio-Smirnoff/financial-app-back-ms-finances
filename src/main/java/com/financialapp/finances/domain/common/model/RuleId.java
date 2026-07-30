package com.financialapp.finances.domain.common.model;

public record RuleId(Long value) {
    public RuleId {
        IdentifiersValidator.requirePositiveIdentifier(value, "ruleId");
    }
}
