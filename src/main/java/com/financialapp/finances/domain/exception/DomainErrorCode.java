package com.financialapp.finances.domain.exception;

/**
 * Stable, framework-neutral domain error codes. Each carries an {@link ErrorCategory}
 * (mapped to HTTP only in web/error) and a wire {@code code} string for the API envelope.
 */
public enum DomainErrorCode {

    // --- Value-object validation ---
    UNSUPPORTED_CURRENCY(ErrorCategory.UNPROCESSABLE, "unsupported_currency"),
    INVALID_MONEY(ErrorCategory.UNPROCESSABLE, "invalid_money"),
    CURRENCY_MISMATCH(ErrorCategory.UNPROCESSABLE, "currency_mismatch"),
    INVALID_CBU(ErrorCategory.BAD_REQUEST, "invalid_cbu"),
    INVALID_IDENTIFIER(ErrorCategory.BAD_REQUEST, "invalid_identifier"),

    // --- Category ---
    INVALID_CATEGORY_NAME(ErrorCategory.BAD_REQUEST, "invalid_category_name"),
    SUBCATEGORY_NOT_IN_CATEGORY(ErrorCategory.NOT_FOUND, "subcategory_not_in_category"),

    // --- Transaction ---
    SAME_ACCOUNT_TRANSACTION(ErrorCategory.BAD_REQUEST, "same_account_transaction"),
    ACCOUNT_CURRENCY_MISMATCH(ErrorCategory.BAD_REQUEST, "account_currency_mismatch"),
    TRANSACTION_NOT_OWNED(ErrorCategory.UNPROCESSABLE, "transaction_not_owned");

    private final ErrorCategory category;
    private final String code;

    DomainErrorCode(ErrorCategory category, String code) {
        this.category = category;
        this.code = code;
    }

    public ErrorCategory getCategory() { return category; }
    public String getCode() { return code; }
}
