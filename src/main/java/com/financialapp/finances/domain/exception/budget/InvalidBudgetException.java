package com.financialapp.finances.domain.exception.budget;

import com.financialapp.commons.core.error.DomainException;
import com.financialapp.finances.domain.exception.DomainErrorCode;

import java.util.Map;

public class InvalidBudgetException extends DomainException {
    public InvalidBudgetException(String message) {
        super(DomainErrorCode.INVALID_BUDGET, message, Map.of());
    }
}
