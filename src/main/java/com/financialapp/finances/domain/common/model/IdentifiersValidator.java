package com.financialapp.finances.domain.common.model;

import com.financialapp.finances.domain.exception.InvalidIdentifierException;

/**
 * Shared validation for identity value objects. The single source of truth for the
 * "non-null, positive Long" rule so CategoryId and UserId do not duplicate it.
 */
final class IdentifiersValidator {

    private IdentifiersValidator() {}

    static long requirePositiveIdentifier(Long value, String field) {
        if (value == null || value <= 0) {
            throw new InvalidIdentifierException(field, value);
        }
        return value;
    }
}
