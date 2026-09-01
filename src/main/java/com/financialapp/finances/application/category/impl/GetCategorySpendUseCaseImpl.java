package com.financialapp.finances.application.category.impl;

import com.financialapp.commons.core.domain.model.Cbu;

import com.financialapp.finances.domain.common.model.*;
import com.financialapp.finances.domain.gateway.AccountOwnershipGateway;
import com.financialapp.finances.domain.model.category.CategoryNames;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.model.transaction.TransactionKind;
import com.financialapp.finances.domain.repository.CategoryRepository;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.domain.service.TransactionClassifier;
import com.financialapp.finances.domain.usecase.category.CategorySpend;
import com.financialapp.finances.domain.usecase.category.GetCategorySpend;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetCategorySpendUseCaseImpl implements GetCategorySpend {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final AccountOwnershipGateway ownershipGateway;
    private final TransactionClassifier classifier = new TransactionClassifier();

    @Override
    @Transactional(readOnly = true)
    public List<CategorySpend> execute(UserId userId, DateRange dateRange, TransactionKind kind) {
        List<Transaction> transactions = dateRange != null
                ? transactionRepository.findByUserAndDateBetween(userId, dateRange.from(), dateRange.to())
                : transactionRepository.findByUser(userId);
        Set<Cbu> ownedAccounts = ownershipGateway.ownedAccounts(userId);

        record Key(CategoryId categoryId, Currency currency) {}
        Map<Key, BigDecimal> totals = new LinkedHashMap<>();
        Map<Key, Long> counts = new LinkedHashMap<>();

        for (Transaction tx : transactions) {
            TransactionKind txKind = classifier.classify(tx, ownedAccounts);
            if (kind != null && txKind != kind) continue;

            Key key = new Key(tx.categoryId(), tx.money().currency());
            totals.merge(key, tx.money().amount(), BigDecimal::add);
            counts.merge(key, 1L, Long::sum);
        }

        return totals.entrySet().stream()
                .map(e -> {
                    Key k = e.getKey();
                    String name = categoryRepository.findNamesById(k.categoryId())
                            .map(n -> n.subcategory() != null ? n.subcategory() : n.category())
                            .orElse("Unknown");
                    return new CategorySpend(k.categoryId(), name,
                            new Money(e.getValue(), k.currency()), counts.get(k));
                })
                .toList();
    }
}
