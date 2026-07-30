package com.financialapp.finances.web.dto.response;

import com.financialapp.finances.domain.model.transaction.PaymentMethod;
import com.financialapp.finances.domain.model.transaction.TransactionKind;
import java.time.LocalDate;

public record TransactionResponse(
        Long id,
        Long userId,
        String fromCbu,
        String toCbu,
        String amount,
        String currency,
        TransactionKind kind,
        Long categoryId,
        String categoryName,
        String description,
        LocalDate date,
        PaymentMethod paymentMethod,
        String note) {
    public TransactionResponse(Long id, Long userId, String fromCbu, String toCbu, String amount,
                               String currency, TransactionKind kind, Long categoryId,
                               String categoryName, String description, LocalDate date) {
        this(id, userId, fromCbu, toCbu, amount, currency, kind, categoryId, categoryName, description, date, PaymentMethod.OTHER, null);
    }
}
