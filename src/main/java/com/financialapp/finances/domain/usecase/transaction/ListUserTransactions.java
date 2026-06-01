package com.financialapp.finances.domain.usecase.transaction;

import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.transaction.ClassifiedTransaction;

import java.util.List;

/** A user's transactions, each paired with its owned-set-derived kind. */
public interface ListUserTransactions {
    List<ClassifiedTransaction> execute(UserId userId);
}
