package com.financialapp.finances.domain.usecase.transaction;

import com.financialapp.finances.domain.usecase.transaction.command.DeleteTransactionCommand;

/** Deletes a transaction owned by the user. */
public interface DeleteTransaction {
    void execute(DeleteTransactionCommand command);
}
