package com.financialapp.finances.domain.exception;

import java.util.Map;

public class InvalidCbuException extends DomainException {
    public InvalidCbuException(String value) {
        super(DomainErrorCode.INVALID_CBU,
              "Invalid CBU: '" + value + "'. Expected exactly 22 digits.",
              Map.of("value", String.valueOf(value)));
    }
}
