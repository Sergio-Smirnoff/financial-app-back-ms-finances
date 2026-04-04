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
public class CardExpenseResponse {
    private Long id;
    private Long userId;
    private Long cardId;
    private String description;
    private BigDecimal totalAmount;
    private String currency;
    private int totalInstallments;
    private int remainingInstallments;
    private BigDecimal installmentAmount;
    private LocalDate nextDueDate;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
