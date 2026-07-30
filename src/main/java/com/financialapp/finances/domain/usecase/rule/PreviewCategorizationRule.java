package com.financialapp.finances.domain.usecase.rule;

import com.financialapp.finances.domain.common.model.RuleId;
import com.financialapp.finances.domain.common.model.UserId;

public interface PreviewCategorizationRule {
    RulePreviewResult execute(RuleId ruleId, UserId userId);
}
