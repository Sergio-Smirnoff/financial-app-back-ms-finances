package com.financialapp.finances.application.transaction.impl;

import com.financialapp.commons.core.domain.model.PageResult;
import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.common.model.OwnedAccount;
import com.financialapp.finances.domain.gateway.AccountOwnershipGateway;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.model.transaction.TransactionFilter;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.domain.usecase.transaction.ListTransactionsFiltered;
import com.financialapp.finances.domain.usecase.transaction.command.TransactionFilterCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListTransactionsFilteredUseCaseImpl implements ListTransactionsFiltered {

    private final TransactionRepository transactionRepository;
    private final AccountOwnershipGateway ownershipGateway;

    @Override
    @Transactional(readOnly = true)
    public PageResult<Transaction> execute(TransactionFilterCommand command) {
        Set<OwnedAccount> owned = ownershipGateway.ownedAccountsWithCurrency(command.userId());
        Set<Cbu> ownedCbus = owned.stream().map(OwnedAccount::cbu).collect(Collectors.toUnmodifiableSet());

        TransactionFilter filter = new TransactionFilter(
                command.userId(),
                ownedCbus,
                command.accountCbu(),
                command.categoryId(),
                command.dateRange(),
                command.kind(),
                command.onlyUncategorised(),
                command.amountMin(),
                command.amountMax()
        );

        return transactionRepository.findFiltered(filter, command.page());
    }
}
