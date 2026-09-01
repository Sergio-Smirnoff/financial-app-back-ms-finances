package com.financialapp.finances.application.transaction.impl;

import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.domain.usecase.transaction.CountUncategorisedTransactions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CountUncategorisedTransactionsUseCaseImpl implements CountUncategorisedTransactions {

    private final TransactionRepository transactionRepository;

    @Override
    @Transactional(readOnly = true)
    public long execute(UserId userId) {
        return transactionRepository.countUncategorised(userId);
    }
}
