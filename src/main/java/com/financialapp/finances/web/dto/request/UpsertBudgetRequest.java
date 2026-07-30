package com.financialapp.finances.web.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UpsertBudgetRequest(
        @NotBlank String amount,
        @NotBlank String currency,
        String alertThresholdPct,
        @Min(1) int year,
        @Min(1) int month
) { }
