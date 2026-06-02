package com.financialapp.finances.domain.usecase.transaction;

import com.financialapp.finances.domain.common.model.DateRange;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.transaction.TransactionSummary;

import java.util.List;

/** A user's income/expense/balance totals, one {@link TransactionSummary} per currency. */
public interface GetTransactionSummary {
    List<TransactionSummary> execute(UserId userId);

    /** Totals restricted to transactions within the given range, one summary per currency. */
    List<TransactionSummary> execute(UserId userId, DateRange range);
}
