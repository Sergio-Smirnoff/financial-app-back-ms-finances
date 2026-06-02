package com.financialapp.finances.domain.repository;

import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.common.model.TransactionId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.transaction.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Persistence boundary for the Transaction aggregate. Aggregate-root only; reads return fully
 * reconstituted aggregates (the read side computes the owned-set-derived label on top).
 */
public interface TransactionRepository {

    Transaction save(Transaction transaction);

    Optional<Transaction> findByIdOwnedBy(TransactionId id, UserId userId);

    /** All of a user's transactions, newest first. */
    List<Transaction> findByUser(UserId userId);

    /** A user's transactions within [from, to] inclusive, newest first. */
    List<Transaction> findByUserAndDateBetween(UserId userId, LocalDate from, LocalDate to);

    /** Transactions touching one account (from OR to), newest first, optionally date-bounded/limited. */
    List<Transaction> findByAccount(Cbu accountCbu, Integer limit, LocalDate from, LocalDate to);

    /** True if the user already has a transaction with the same from/to, amount, currency, date and description. */
    boolean existsDuplicate(UserId userId, Cbu fromCbu, Cbu toCbu,
                            BigDecimal amount, String currency, LocalDate date, String description);

    void delete(Transaction transaction);
}
