package com.financialapp.finances.application.transaction.impl;

import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.gateway.AccountOwnershipGateway;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.model.transaction.TransactionKind;
import com.financialapp.finances.domain.model.transaction.TransactionSummary;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.domain.service.TransactionClassifier;
import com.financialapp.finances.domain.usecase.transaction.GetTransactionSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GetTransactionSummaryUseCaseImpl implements GetTransactionSummary {

    private final TransactionRepository transactionRepository;
    private final AccountOwnershipGateway ownershipGateway;
    private final TransactionClassifier classifier;

    @Override
    @Transactional(readOnly = true)
    public TransactionSummary execute(UserId userId) {
        Set<Cbu> owned = ownershipGateway.ownedAccounts(userId);
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;
        String currency = null;
        for (Transaction tx : transactionRepository.findByUser(userId)) {
            TransactionKind kind = classifier.classify(tx, owned);
            currency = tx.money().currency().getCurrencyCode();
            if (kind == TransactionKind.EXPENSE) {
                expense = expense.add(tx.money().amount());
            } else if (kind == TransactionKind.INCOME) {
                income = income.add(tx.money().amount());
            }
        }
        return new TransactionSummary(currency, income, expense, income.subtract(expense));
    }
}
