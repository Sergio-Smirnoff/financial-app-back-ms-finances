package com.financialapp.finances.domain.exception.transaction;

import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.exception.DomainErrorCode;
import com.financialapp.finances.domain.exception.DomainException;

import java.util.Map;

/**
 * A transaction must move money between two distinct accounts; the same CBU was given on
 * both sides.
 */
public class SameAccountTransactionException extends DomainException {
    public SameAccountTransactionException(Cbu cbu) {
        super(DomainErrorCode.SAME_ACCOUNT_TRANSACTION,
              "A transaction cannot have the same account on both sides: " + cbu.cbuNumber(),
              Map.of("cbu", cbu.cbuNumber()));
    }
}
