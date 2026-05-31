package com.financialapp.finances.domain.common.model;

/**
 * Identity of the owning user. Reifies the bare {@code Long userId} used across the legacy code.
 */
public record UserId(Long value) {
    public UserId {
        IdentifiersValidator.requirePositiveIdentifier(value, "userId");
    }
}
