package com.financialapp.finances.domain.usecase.rule;

import com.financialapp.finances.domain.common.model.UserId;

import java.util.List;

public interface ListCategorizationRules {
    List<RuleView> execute(UserId userId);
}
