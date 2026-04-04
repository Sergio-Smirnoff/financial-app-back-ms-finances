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
public class LoanResponse {
    private Long id;
    private Long userId;
    private String description;
    private String entity;
    private BigDecimal totalAmount;
    private String currency;
    private int totalInstallments;
    private int paidInstallments;
    private LocalDate nextPaymentDate;
    private BigDecimal installmentAmount;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
