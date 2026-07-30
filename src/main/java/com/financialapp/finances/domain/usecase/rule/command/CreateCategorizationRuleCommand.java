package com.financialapp.finances.domain.usecase.rule.command;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.rule.RuleMatchType;

public record CreateCategorizationRuleCommand(
        UserId userId,
        RuleMatchType matchType,
        String pattern,
        CategoryId categoryId
) { }
