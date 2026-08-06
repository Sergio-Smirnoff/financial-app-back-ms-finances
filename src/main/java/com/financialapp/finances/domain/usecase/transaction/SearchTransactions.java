package com.financialapp.finances.domain.usecase.transaction;

import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.transaction.Transaction;

import java.util.List;

/** Case-insensitive keyword search over a user's transaction descriptions. */
public interface SearchTransactions {
    List<Transaction> execute(UserId userId, String query, int limit);
}
