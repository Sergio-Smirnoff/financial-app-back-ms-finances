package com.financialapp.finances.application.transaction.impl;

import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.domain.usecase.transaction.SearchTransactions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchTransactionsUseCaseImpl implements SearchTransactions {

    private final TransactionRepository transactionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> execute(UserId userId, String query, int limit) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("query must not be blank");
        }
        int effectiveLimit = limit <= 0 ? 10 : Math.min(limit, 50);
        return transactionRepository.searchByDescription(userId, query.trim(), effectiveLimit);
    }
}
