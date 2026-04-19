package com.financialapp.finances.model.dto.request;

import com.financialapp.finances.model.enums.TransactionType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class TransactionRequest {

    @NotNull(message = "Type is required")
    private TransactionType type;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private Long accountId;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "ARS|USD", message = "Currency must be ARS or USD")
    private String currency;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Date is required")
    private LocalDate date;
}
