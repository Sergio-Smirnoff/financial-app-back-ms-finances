package com.financialapp.finances.domain.usecase.transaction;

import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.transaction.TransactionSummary;

/** A user's income/expense/balance totals. */
public interface GetTransactionSummary {
    TransactionSummary execute(UserId userId);
}
