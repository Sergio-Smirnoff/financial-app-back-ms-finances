package com.financialapp.finances.domain.usecase.rule;

import com.financialapp.finances.domain.common.model.RuleId;
import com.financialapp.finances.domain.common.model.UserId;

public interface DeleteCategorizationRule {
    void execute(RuleId ruleId, UserId userId);
}
