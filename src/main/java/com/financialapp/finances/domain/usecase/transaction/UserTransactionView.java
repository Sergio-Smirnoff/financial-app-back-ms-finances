package com.financialapp.finances.domain.usecase.transaction;

import com.financialapp.finances.domain.model.category.CategoryNames;
import com.financialapp.finances.domain.model.transaction.ClassifiedTransaction;

/** One user-view row: the classified transaction plus its resolved category display names. */
public record UserTransactionView(ClassifiedTransaction classified, CategoryNames names) {}
