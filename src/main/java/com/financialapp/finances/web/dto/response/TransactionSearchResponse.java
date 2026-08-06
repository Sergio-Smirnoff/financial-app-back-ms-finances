package com.financialapp.finances.web.dto.response;

import java.time.LocalDate;

/**
 * Wire DTO for a transaction matching a search query.
 */
public record TransactionSearchResponse(
        Long id,
        LocalDate date,
        String description,
        String amount,
        String currency,
        String direction
) {
}
