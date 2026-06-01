package com.financialapp.finances.domain.usecase.transaction;

import com.financialapp.finances.domain.model.category.CategoryNames;
import com.financialapp.finances.domain.model.transaction.Transaction;

/** One account-view row: the transaction plus its resolved category display names. */
public record AccountTransactionView(Transaction transaction, CategoryNames names) {}
