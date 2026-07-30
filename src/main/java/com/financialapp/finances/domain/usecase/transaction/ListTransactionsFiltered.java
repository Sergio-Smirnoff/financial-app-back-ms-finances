package com.financialapp.finances.domain.usecase.transaction;

import com.financialapp.commons.core.domain.model.PageResult;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.usecase.transaction.command.TransactionFilterCommand;

public interface ListTransactionsFiltered {
    PageResult<Transaction> execute(TransactionFilterCommand command);
}
