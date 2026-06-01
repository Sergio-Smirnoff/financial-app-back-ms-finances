package com.financialapp.finances.domain.usecase.transaction;

import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.usecase.transaction.command.UpdateTransactionCommand;

/** Updates an existing transaction's editable details and returns the persisted aggregate. */
public interface UpdateTransaction {
    Transaction execute(UpdateTransactionCommand command);
}
