package com.financialapp.finances.web.dto.response;

import java.math.BigDecimal;

public record TransactionSummaryResponse(
        String currency,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance) {}
