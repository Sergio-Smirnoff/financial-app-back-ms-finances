package com.financialapp.finances.domain.exception.transaction;

import com.financialapp.commons.core.error.DomainException;
import com.financialapp.finances.domain.common.model.TransactionId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.exception.DomainErrorCode;

import java.util.Map;

public class TransactionNotFoundException extends DomainException {
    public TransactionNotFoundException(TransactionId id, UserId userId) {
        super(DomainErrorCode.TRANSACTION_NOT_FOUND,
              "Transaction " + id.value() + " not found for user " + userId.value(),
              Map.of("transactionId", id.value(), "userId", userId.value()));
    }
}
