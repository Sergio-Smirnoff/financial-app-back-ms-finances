package com.financialapp.finances.application.transaction.impl;

import com.financialapp.finances.domain.common.model.TransactionId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.exception.transaction.TransactionNotFoundException;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.domain.usecase.transaction.GetTransactionDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetTransactionDetailUseCaseImpl implements GetTransactionDetail {

    private final TransactionRepository transactionRepository;

    @Override
    @Transactional(readOnly = true)
    public Transaction execute(TransactionId id, UserId userId) {
        return transactionRepository.findByIdOwnedBy(id, userId)
                .orElseThrow(() -> new TransactionNotFoundException(id, userId));
    }
}
