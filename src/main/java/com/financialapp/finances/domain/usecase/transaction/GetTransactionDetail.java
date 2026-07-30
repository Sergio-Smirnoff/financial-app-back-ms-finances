package com.financialapp.finances.domain.usecase.transaction;

import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.common.model.TransactionId;
import com.financialapp.finances.domain.common.model.UserId;

public interface GetTransactionDetail {
    Transaction execute(TransactionId id, UserId userId);
}
