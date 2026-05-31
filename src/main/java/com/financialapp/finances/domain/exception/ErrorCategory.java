package com.financialapp.finances.domain.exception;

/**
 * Framework-neutral classification of a domain error. The web layer maps this to an HTTP
 * status; the domain stays HTTP-agnostic.
 */
public enum ErrorCategory {
    NOT_FOUND,
    CONFLICT,
    UNPROCESSABLE,
    BAD_REQUEST,
    INTERNAL_SERVER_ERROR
}
