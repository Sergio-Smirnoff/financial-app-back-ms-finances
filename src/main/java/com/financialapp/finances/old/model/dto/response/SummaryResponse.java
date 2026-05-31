package com.financialapp.finances.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryResponse {
    private String currency;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;
    private int activeLoans;
    private BigDecimal totalLoanDebt;
    private int activeCardExpenses;
    private BigDecimal totalCardExpenseDebt;
}
