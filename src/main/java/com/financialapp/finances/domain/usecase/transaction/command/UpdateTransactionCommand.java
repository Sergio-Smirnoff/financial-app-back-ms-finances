package com.financialapp.finances.domain.usecase.transaction.command;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.TransactionId;
import com.financialapp.finances.domain.common.model.UserId;

import java.time.LocalDate;

/**
 * Partial edit of an existing transaction. The amount, currency and the two accounts are frozen;
 * only category, description, date and note are editable. Each editable field is optional: a {@code null}
 * field means "leave unchanged".
 */
public record UpdateTransactionCommand(
        UserId userId,
        TransactionId id,
        CategoryId categoryId,
        String description,
        LocalDate date,
        String note) {
    public UpdateTransactionCommand(UserId userId, TransactionId id, CategoryId categoryId,
                                    String description, LocalDate date) {
        this(userId, id, categoryId, description, date, null);
    }
}
