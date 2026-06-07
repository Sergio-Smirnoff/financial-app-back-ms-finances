package com.financialapp.finances.infrastructure.messaging.payload;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentRecordedData(
        Long userId,
        String accountCbu,
        BigDecimal amount,
        String currency,
        String description,
        LocalDate date) {}
