package com.financialapp.finances.web.dto.response;

/**
 * Wire DTO for monthly income and expense totals.
 */
public record MonthlyFlowResponse(
        String month,
        String currency,
        String income,
        String expense
) {
}
