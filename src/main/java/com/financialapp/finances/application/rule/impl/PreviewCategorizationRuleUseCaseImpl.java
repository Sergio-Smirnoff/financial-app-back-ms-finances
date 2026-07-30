package com.financialapp.finances.application.rule.impl;

import com.financialapp.finances.domain.common.model.RuleId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.exception.rule.InvalidCategorizationRuleException;
import com.financialapp.finances.domain.model.rule.CategorizationRule;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.repository.CategorizationRuleRepository;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.domain.usecase.rule.PreviewCategorizationRule;
import com.financialapp.finances.domain.usecase.rule.RulePreviewResult;
import com.financialapp.finances.infrastructure.persistence.repository.SystemCategoryResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PreviewCategorizationRuleUseCaseImpl implements PreviewCategorizationRule {

    private final CategorizationRuleRepository ruleRepository;
    private final TransactionRepository transactionRepository;
    private final SystemCategoryResolver systemCategoryResolver;

    @Override
    @Transactional(readOnly = true)
    public RulePreviewResult execute(RuleId ruleId, UserId userId) {
        CategorizationRule rule = ruleRepository.findByIdOwnedBy(ruleId, userId)
                .orElseThrow(() -> new InvalidCategorizationRuleException("Categorization rule not found or not owned by user"));

        Optional<Long> unassignedIdOpt = systemCategoryResolver.findUnassignedCategoryId();
        if (unassignedIdOpt.isEmpty()) {
            return new RulePreviewResult(0, List.of());
        }
        Long unassignedId = unassignedIdOpt.get();

        List<Transaction> matchingTransactions = transactionRepository.findByUser(userId).stream()
                .filter(tx -> unassignedId.equals(tx.categoryId().value()))
                .filter(tx -> rule.matches(tx.description()))
                .toList();

        List<Long> sampleIds = matchingTransactions.stream()
                .map(tx -> tx.id().value())
                .limit(10)
                .toList();

        return new RulePreviewResult(matchingTransactions.size(), sampleIds);
    }
}
