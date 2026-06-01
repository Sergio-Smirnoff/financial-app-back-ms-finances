package com.financialapp.finances.domain.repository;

import com.financialapp.finances.domain.common.model.TransactionId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.transaction.Transaction;

import java.util.Optional;

/**
 * Persistence boundary for the Transaction aggregate. Returns aggregates only. List, summary and
 * duplicate-check reads are owned-set projections and are added in the read/query slice, not here.
 */
public interface TransactionRepository {

    Transaction save(Transaction transaction);

    Optional<Transaction> findByIdOwnedBy(TransactionId id, UserId userId);
}
