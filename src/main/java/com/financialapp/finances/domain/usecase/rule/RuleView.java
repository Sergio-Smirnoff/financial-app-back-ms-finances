package com.financialapp.finances.domain.usecase.rule;

import com.financialapp.finances.domain.model.rule.CategorizationRule;

public record RuleView(CategorizationRule rule, String categoryName) { }
