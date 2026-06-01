package com.financialapp.finances.application.transaction.impl;

import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.gateway.AccountOwnershipGateway;
import com.financialapp.finances.domain.model.transaction.ClassifiedTransaction;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.domain.service.TransactionClassifier;
import com.financialapp.finances.domain.usecase.transaction.ListUserTransactions;
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

    @Override
    @Transactional(readOnly = true)
    public List<ClassifiedTransaction> execute(UserId userId) {
        Set<Cbu> owned = ownershipGateway.ownedAccounts(userId);
        return transactionRepository.findByUser(userId).stream()
                .map(tx -> new ClassifiedTransaction(tx, classifier.classify(tx, owned)))
                .toList();
    }
}
