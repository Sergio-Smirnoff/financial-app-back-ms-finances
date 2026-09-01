package com.financialapp.finances.domain.repository;

import com.financialapp.finances.domain.common.model.RuleId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.rule.CategorizationRule;

import java.util.List;
import java.util.Optional;

public interface CategorizationRuleRepository {
    CategorizationRule save(CategorizationRule rule);
    List<CategorizationRule> findByUser(UserId userId);
    Optional<CategorizationRule> findByIdOwnedBy(RuleId ruleId, UserId userId);
    void delete(CategorizationRule rule);
}
