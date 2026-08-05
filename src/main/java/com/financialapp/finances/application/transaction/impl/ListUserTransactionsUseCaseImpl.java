package com.financialapp.finances.application.transaction.impl;

import com.financialapp.commons.core.domain.model.Cbu;
import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.gateway.AccountOwnershipGateway;
import com.financialapp.finances.domain.model.category.CategoryNames;
import com.financialapp.finances.domain.model.transaction.ClassifiedTransaction;
import com.financialapp.finances.domain.repository.CategoryRepository;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.domain.service.TransactionClassifier;
import com.financialapp.finances.domain.usecase.transaction.ListUserTransactions;
import com.financialapp.finances.domain.usecase.transaction.UserTransactionView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ListUserTransactionsUseCaseImpl implements ListUserTransactions {

    private final TransactionRepository transactionRepository;
    private final AccountOwnershipGateway ownershipGateway;
    private final TransactionClassifier classifier;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserTransactionView> execute(UserId userId) {
        Set<Cbu> owned = ownershipGateway.ownedAccounts(userId);
        return transactionRepository.findByUser(userId).stream()
                .map(tx -> new UserTransactionView(
                        new ClassifiedTransaction(tx, classifier.classify(tx, owned)),
                        resolveNames(tx.categoryId())))
                .toList();
    }

    private CategoryNames resolveNames(CategoryId categoryId) {
        return categoryRepository.findNamesById(categoryId).orElse(new CategoryNames(null, null));
    }
}
