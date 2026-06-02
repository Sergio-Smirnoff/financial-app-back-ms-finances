package com.financialapp.finances.web.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record RecordTransactionRequest(
        @NotBlank @Pattern(regexp = "\\d{22}") String fromCbu,
        @NotBlank @Pattern(regexp = "\\d{22}") String toCbu,
        @NotBlank @Pattern(regexp = "\\d+(\\.\\d+)?", message = "amount must be a positive decimal string") String amount,
        @NotBlank @SupportedCurrency String currency,
        @NotNull Long categoryId,
        String description,
        @NotNull LocalDate date) {}
