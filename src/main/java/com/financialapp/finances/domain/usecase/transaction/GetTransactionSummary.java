package com.financialapp.finances.domain.usecase.transaction;

import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.transaction.TransactionSummary;

import java.util.List;

/** A user's income/expense/balance totals, one {@link TransactionSummary} per currency. */
public interface GetTransactionSummary {
    List<TransactionSummary> execute(UserId userId);
}
