package com.financialapp.finances.application.transaction.impl;

import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.domain.usecase.transaction.ListAccountTransactions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ListAccountTransactionsUseCaseImpl implements ListAccountTransactions {

    private final TransactionRepository transactionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> execute(Cbu accountCbu, Integer limit, LocalDate from, LocalDate to) {
        return transactionRepository.findByAccount(accountCbu, limit, from, to);
    }
}
