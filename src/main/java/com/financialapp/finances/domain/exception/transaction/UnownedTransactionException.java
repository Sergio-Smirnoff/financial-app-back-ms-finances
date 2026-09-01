package com.financialapp.finances.domain.exception.transaction;

import com.financialapp.commons.core.domain.model.Cbu;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.exception.DomainErrorCode;
import com.financialapp.commons.core.error.DomainException;

import java.util.Map;

/**
 * Raised when a transaction touches no account owned by the user — it cannot be placed in
 * this user's ledger. Ownership is decided by ms-banks, never trusted from the client.
 */
public class UnownedTransactionException extends DomainException {
    public UnownedTransactionException(UserId userId, Cbu fromCbu, Cbu toCbu) {
        super(DomainErrorCode.TRANSACTION_NOT_OWNED,
              "Transaction touches no account owned by user " + userId.value()
                  + " (from=" + fromCbu.cbuNumber() + ", to=" + toCbu.cbuNumber() + ")",
              Map.of("userId", userId.value(),
                     "fromCbu", fromCbu.cbuNumber(),
                     "toCbu", toCbu.cbuNumber()));
    }
}
