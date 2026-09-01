package com.financialapp.finances.domain.model.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public record FxSnapshot(
        BigDecimal mepRate,
        BigDecimal cclRate,
        BigDecimal oficialRate,
        LocalDate rateDate
) {
    public FxSnapshot {
        Objects.requireNonNull(rateDate, "rateDate must not be null");
        if (mepRate != null && mepRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("mepRate must not be negative");
        }
        if (cclRate != null && cclRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("cclRate must not be negative");
        }
        if (oficialRate != null && oficialRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("oficialRate must not be negative");
        }
    }
}
