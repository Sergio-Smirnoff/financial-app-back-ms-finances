package com.financialapp.finances.web.dto.response;

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
        LocalDate date) {}
