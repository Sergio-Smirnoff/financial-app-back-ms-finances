package com.financialapp.finances.domain.exception.rule;

import com.financialapp.commons.core.error.DomainException;
import com.financialapp.finances.domain.exception.DomainErrorCode;

import java.util.Map;

public class InvalidCategorizationRuleException extends DomainException {
    public InvalidCategorizationRuleException(String message) {
        super(DomainErrorCode.INVALID_CATEGORIZATION_RULE, message, Map.of());
    }
}
