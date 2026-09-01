package com.financialapp.finances.application.transaction.impl;

import com.financialapp.commons.core.domain.model.Cbu;
import com.financialapp.finances.domain.gateway.AccountOwnershipGateway;
import com.financialapp.finances.domain.gateway.DomainEventPublisher;
import com.financialapp.finances.domain.model.transaction.BalanceMovement;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.domain.service.TransactionPosting;
import com.financialapp.finances.domain.usecase.transaction.DeleteTransaction;
import com.financialapp.finances.domain.usecase.transaction.command.DeleteTransactionCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DeleteTransactionUseCaseImpl implements DeleteTransaction {

    private final TransactionRepository transactionRepository;
    private final AccountOwnershipGateway ownershipGateway;
    private final TransactionPosting transactionPosting;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    @Transactional
    public void execute(DeleteTransactionCommand command) {
        Transaction existing = transactionRepository
                .findByIdOwnedBy(command.id(), command.userId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "transaction " + command.id().value() + " not found for user"));

        Set<Cbu> owned = ownershipGateway.ownedAccounts(command.userId());
        List<BalanceMovement> movements = transactionPosting.post(existing, owned);

        existing.recordReversal(movements);
        domainEventPublisher.publishAll(existing.pullDomainEvents());
        transactionRepository.delete(existing);
    }
}
