package com.financialapp.finances.web.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

/**
 * Partial edit of a transaction's user-editable fields. Every field is optional; a {@code null}
 * field means "leave unchanged". Amount and currency are immutable and cannot be edited here, so
 * an edit never moves an account balance. At least one editable field must be present.
 */
public record UpdateTransactionRequest(
        @Positive Long categoryId,
        String description,
        LocalDate date,
        String note) {

    @AssertTrue(message = "At least one of categoryId, description, date or note must be provided")
    public boolean isAtLeastOneFieldPresent() {
        return categoryId != null || description != null || date != null || note != null;
    }
}
