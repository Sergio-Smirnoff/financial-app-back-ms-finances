package com.financialapp.finances.domain.usecase.transaction;

import com.financialapp.finances.domain.common.model.UserId;

import java.util.List;

/** A user's transactions, each paired with its owned-set-derived kind and resolved category names. */
public interface ListUserTransactions {
    List<UserTransactionView> execute(UserId userId);
}
