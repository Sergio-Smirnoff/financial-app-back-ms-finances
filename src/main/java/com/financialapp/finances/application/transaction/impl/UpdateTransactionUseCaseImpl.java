package com.financialapp.finances.application.transaction.impl;

import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.domain.usecase.transaction.UpdateTransaction;
import com.financialapp.finances.domain.usecase.transaction.command.UpdateTransactionCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateTransactionUseCaseImpl implements UpdateTransaction {

    private final TransactionRepository transactionRepository;

    /**
     * Apply a partial edit to category/description/date. Amount, currency and the two accounts are
     * frozen, so the balance never moves and no ms-banks events are emitted. A {@code null} field on
     * the command leaves the existing value unchanged.
     */
    @Override
    @Transactional
    public Transaction execute(UpdateTransactionCommand command) {
        Transaction existing = transactionRepository
                .findByIdOwnedBy(command.id(), command.userId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "transaction " + command.id().value() + " not found for user"));

        Transaction updated = existing.changeDetails(
                command.categoryId() != null ? command.categoryId() : existing.categoryId(),
                command.description() != null ? command.description() : existing.description(),
                command.date() != null ? command.date() : existing.date());

        return transactionRepository.save(updated);
    }
}
