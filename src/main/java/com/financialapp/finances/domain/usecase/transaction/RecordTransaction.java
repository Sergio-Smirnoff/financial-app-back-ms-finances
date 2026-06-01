package com.financialapp.finances.domain.usecase.transaction;

import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.usecase.transaction.command.RecordTransactionCommand;

/** Records a transaction (expense / income / transfer) and returns the persisted aggregate. */
public interface RecordTransaction {
    Transaction execute(RecordTransactionCommand command);
}
