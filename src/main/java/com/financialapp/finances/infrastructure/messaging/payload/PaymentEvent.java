package com.financialapp.finances.infrastructure.messaging.payload;

import java.math.BigDecimal;
import java.time.LocalDate;

/** ms-banks → finances ledger event (consumed). One account; direction inferred from description. */
public record PaymentEvent(
        Long userId,
        String accountCbu,
        BigDecimal amount,
        String currency,
        String description,
        LocalDate date) {}
