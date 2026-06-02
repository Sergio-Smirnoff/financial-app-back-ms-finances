package com.financialapp.finances.application.transaction.impl;

import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.common.model.OwnedAccount;
import com.financialapp.finances.domain.gateway.AccountOwnershipGateway;
import com.financialapp.finances.domain.gateway.DomainEventPublisher;
import com.financialapp.finances.domain.model.transaction.BalanceMovement;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.domain.service.TransactionCurrencyValidator;
import com.financialapp.finances.domain.service.TransactionPosting;
import com.financialapp.finances.domain.usecase.transaction.RecordTransaction;
import com.financialapp.finances.domain.usecase.transaction.command.RecordTransactionCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Records a transaction and emits one balance event per owned side. The aggregate records its own
 * {@code TransactionCreated} events (DDD: events are recorded by aggregates, drained by the use
 * case) — this class never constructs a domain event, so the application layer stays free of the
 * {@code domain.event} package (enforced by {@code LayeredArchitectureTest}).
 */
@Service
@RequiredArgsConstructor
public class RecordTransactionUseCaseImpl implements RecordTransaction {

    private final TransactionRepository transactionRepository;
    private final AccountOwnershipGateway ownershipGateway;
    private final TransactionPosting transactionPosting;
    private final TransactionCurrencyValidator currencyValidator;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    @Transactional
    public Transaction execute(RecordTransactionCommand command) {
        Set<OwnedAccount> owned = ownershipGateway.ownedAccountsWithCurrency(command.userId());

        Transaction tx = Transaction.create(
                command.userId(), command.fromCbu(), command.toCbu(),
                command.money(), command.categoryId(), command.description(), command.date());

        // Reject before persisting: every owned side must be held in the transaction's currency.
        currencyValidator.validate(tx, owned);

        // Derive movements — throws UnownedTransactionException before anything persists.
        Set<Cbu> ownedCbus = owned.stream().map(OwnedAccount::cbu).collect(Collectors.toUnmodifiableSet());
        List<BalanceMovement> movements = transactionPosting.post(tx, ownedCbus);

        Transaction saved = transactionRepository.save(tx);

        // The persisted aggregate (id assigned) records one creation event per movement.
        saved.recordCreationEvents(movements);
        domainEventPublisher.publishAll(saved.pullDomainEvents());

        return saved;
    }
}
