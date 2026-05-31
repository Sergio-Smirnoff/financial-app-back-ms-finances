package com.financialapp.finances.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpcomingPaymentResponse {
    private Long sourceId;
    private String type;
    private String description;
    private BigDecimal amount;
    private String currency;
    private LocalDate dueDate;
    private int installmentNumber;
    private int totalInstallments;
    private boolean paid;
}
