package com.financialapp.finances.model.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class CardExpenseRequest {

    @NotNull(message = "Card ID is required")
    private Long cardId;

    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be positive")
    private BigDecimal totalAmount;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "ARS|USD", message = "Currency must be ARS or USD")
    private String currency;

    @NotNull(message = "Total installments is required")
    @Min(value = 1, message = "Total installments must be at least 1")
    private Integer totalInstallments;

    @NotNull(message = "Installment amount is required")
    @Positive(message = "Installment amount must be positive")
    private BigDecimal installmentAmount;

    @NotNull(message = "Next due date is required")
    private LocalDate nextDueDate;
}
