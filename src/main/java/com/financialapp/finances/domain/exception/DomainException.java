package com.financialapp.finances.domain.exception;

import java.util.Map;

/**
 * Base type for all Finances domain errors. Framework-neutral: carries a stable
 * {@link DomainErrorCode} and optional structured details. The web layer maps the code
 * to an HTTP status and wraps it in the ApiResponse envelope.
 */
public abstract class DomainException extends RuntimeException {

    private final DomainErrorCode error;
    private final Map<String, Object> details;

    protected DomainException(DomainErrorCode error, String message) {
        this(error, message, null);
    }

    protected DomainException(DomainErrorCode error, String message, Map<String, Object> details) {
        super(message);
        this.error = error;
        this.details = details;
    }

    public DomainErrorCode getError() { return error; }
    public Map<String, Object> getDetails() { return details; }
}
