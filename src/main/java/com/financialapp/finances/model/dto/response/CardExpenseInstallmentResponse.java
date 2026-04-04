package com.financialapp.finances.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardExpenseInstallmentResponse {
    private Long id;
    private Long cardExpenseId;
    private int installmentNumber;
    private BigDecimal amount;
    private LocalDate dueDate;
    private boolean paid;
    private LocalDate paidDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
