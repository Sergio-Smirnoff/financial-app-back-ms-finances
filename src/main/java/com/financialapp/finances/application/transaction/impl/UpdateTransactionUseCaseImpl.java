package com.financialapp.finances.application.transaction.impl;

import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.gateway.AccountOwnershipGateway;
import com.financialapp.finances.domain.gateway.DomainEventPublisher;
import com.financialapp.finances.domain.model.transaction.BalanceMovement;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.domain.service.TransactionPosting;
import com.financialapp.finances.domain.usecase.transaction.UpdateTransaction;
import com.financialapp.finances.domain.usecase.transaction.command.UpdateTransactionCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UpdateTransactionUseCaseImpl implements UpdateTransaction {

    private final TransactionRepository transactionRepository;
    private final AccountOwnershipGateway ownershipGateway;
    private final TransactionPosting transactionPosting;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    @Transactional
    public Transaction execute(UpdateTransactionCommand command) {
        Transaction existing = transactionRepository
                .findByIdOwnedBy(command.id(), command.userId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "transaction " + command.id().value() + " not found for user"));

        Set<Cbu> owned = ownershipGateway.ownedAccounts(command.userId());
        List<BalanceMovement> oldMovements = transactionPosting.post(existing, owned);

        Transaction updated = existing.changeDetails(
                command.money(), command.categoryId(), command.description(), command.date());
        List<BalanceMovement> newMovements = transactionPosting.post(updated, owned);

        Transaction saved = transactionRepository.save(updated);
        saved.recordCorrection(oldMovements, newMovements);
        domainEventPublisher.publishAll(saved.pullDomainEvents());
        return saved;
    }
}
