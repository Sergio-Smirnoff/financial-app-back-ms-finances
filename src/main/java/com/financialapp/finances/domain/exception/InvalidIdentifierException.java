package com.financialapp.finances.domain.exception;

import java.util.Map;

public class InvalidIdentifierException extends DomainException {
    public InvalidIdentifierException(String field, Object value) {
        super(DomainErrorCode.INVALID_IDENTIFIER,
              "Invalid " + field + ": '" + value + "'. Expected a non-null positive value.",
              Map.of("field", field, "value", String.valueOf(value)));
    }
}
