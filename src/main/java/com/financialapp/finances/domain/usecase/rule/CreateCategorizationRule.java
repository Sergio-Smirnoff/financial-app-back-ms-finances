package com.financialapp.finances.domain.usecase.rule;

import com.financialapp.finances.domain.usecase.rule.command.CreateCategorizationRuleCommand;

public interface CreateCategorizationRule {
    RuleView execute(CreateCategorizationRuleCommand command);
}
