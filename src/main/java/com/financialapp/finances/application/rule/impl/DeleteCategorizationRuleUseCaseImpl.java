package com.financialapp.finances.application.rule.impl;

import com.financialapp.finances.domain.common.model.RuleId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.exception.rule.InvalidCategorizationRuleException;
import com.financialapp.finances.domain.model.rule.CategorizationRule;
import com.financialapp.finances.domain.repository.CategorizationRuleRepository;
import com.financialapp.finances.domain.usecase.rule.DeleteCategorizationRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteCategorizationRuleUseCaseImpl implements DeleteCategorizationRule {

    private final CategorizationRuleRepository ruleRepository;

    @Override
    @Transactional
    public void execute(RuleId ruleId, UserId userId) {
        CategorizationRule rule = ruleRepository.findByIdOwnedBy(ruleId, userId)
                .orElseThrow(() -> new InvalidCategorizationRuleException("Categorization rule not found or not owned by user"));
        ruleRepository.delete(rule);
    }
}
