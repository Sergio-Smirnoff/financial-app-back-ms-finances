package com.financialapp.finances.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransferRequest(
        @NotNull(message = "From account ID is required") Long fromAccountId,
        @NotNull(message = "To account ID is required") Long toAccountId,
        @NotNull(message = "Amount is required") @DecimalMin(value = "0.01", message = "Amount must be at least 0.01") BigDecimal amount,
        @NotBlank(message = "Currency is required") String currency,
        @NotBlank(message = "Description is required") String description,
        @NotNull(message = "Date is required") LocalDate date
) {}
