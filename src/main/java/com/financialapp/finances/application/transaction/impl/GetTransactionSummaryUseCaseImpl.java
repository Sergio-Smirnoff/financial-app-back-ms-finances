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
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GetTransactionSummaryUseCaseImpl implements GetTransactionSummary {

    private final TransactionRepository transactionRepository;
    private final AccountOwnershipGateway ownershipGateway;
    private final TransactionClassifier classifier;

    @Override
    @Transactional(readOnly = true)
    public List<TransactionSummary> execute(UserId userId) {
        Set<Cbu> owned = ownershipGateway.ownedAccounts(userId);
        Map<Currency, Totals> byCurrency = new LinkedHashMap<>();
        for (Transaction tx : transactionRepository.findByUser(userId)) {
            TransactionKind kind = classifier.classify(tx, owned);
            byCurrency.computeIfAbsent(tx.money().currency(), c -> new Totals())
                    .add(kind, tx.money().amount());
        }
        return byCurrency.entrySet().stream()
                .map(e -> e.getValue().toSummary(e.getKey()))
                .toList();
    }

    /** Mutable per-currency accumulator; transfers (neither income nor expense) are ignored. */
    private static final class Totals {
        private BigDecimal income = BigDecimal.ZERO;
        private BigDecimal expense = BigDecimal.ZERO;

        void add(TransactionKind kind, BigDecimal amount) {
            if (kind == TransactionKind.EXPENSE) {
                expense = expense.add(amount);
            } else if (kind == TransactionKind.INCOME) {
                income = income.add(amount);
            }
        }

        TransactionSummary toSummary(Currency currency) {
            return new TransactionSummary(currency, income, expense, income.subtract(expense));
        }
    }
}
