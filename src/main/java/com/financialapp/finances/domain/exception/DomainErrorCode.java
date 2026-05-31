package com.financialapp.finances.domain.exception;

/**
 * Stable, framework-neutral domain error codes. Each carries an {@link ErrorCategory}
 * (mapped to HTTP only in web/error) and a wire {@code code} string for the API envelope.
 */
public enum DomainErrorCode {

    // --- Value-object validation ---
    INVALID_CURRENCY(ErrorCategory.BAD_REQUEST, "invalid_currency"),
    INVALID_MONEY(ErrorCategory.UNPROCESSABLE, "invalid_money"),
    CURRENCY_MISMATCH(ErrorCategory.UNPROCESSABLE, "currency_mismatch"),
    INVALID_CBU(ErrorCategory.BAD_REQUEST, "invalid_cbu"),
    INVALID_IDENTIFIER(ErrorCategory.BAD_REQUEST, "invalid_identifier");

    private final ErrorCategory category;
    private final String code;

    DomainErrorCode(ErrorCategory category, String code) {
        this.category = category;
        this.code = code;
    }

    public ErrorCategory getCategory() { return category; }
    public String getCode() { return code; }
}
