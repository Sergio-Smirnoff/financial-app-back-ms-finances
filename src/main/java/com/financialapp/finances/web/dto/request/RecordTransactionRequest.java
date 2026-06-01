package com.financialapp.finances.web.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RecordTransactionRequest(
        @NotBlank @Pattern(regexp = "\\d{22}") String fromCbu,
        @NotBlank @Pattern(regexp = "\\d{22}") String toCbu,
        @NotNull @Positive BigDecimal amount,
        @NotBlank @SupportedCurrency String currency,
        @NotNull Long categoryId,
        String description,
        @NotNull LocalDate date) {}
