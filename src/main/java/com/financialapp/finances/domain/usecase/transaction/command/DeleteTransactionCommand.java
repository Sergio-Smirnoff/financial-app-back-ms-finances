package com.financialapp.finances.domain.usecase.transaction.command;

import com.financialapp.finances.domain.common.model.TransactionId;
import com.financialapp.finances.domain.common.model.UserId;

public record DeleteTransactionCommand(UserId userId, TransactionId id) {}
