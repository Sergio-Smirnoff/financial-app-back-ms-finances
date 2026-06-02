package com.financialapp.finances.domain.common.model;

import java.time.LocalDate;

/** A closed date interval [from, to]. Reifies the bare from/to pair with the from &lt;= to invariant. */
public record DateRange(LocalDate from, LocalDate to) {
    public DateRange {
        if (from == null || to == null) {
            throw new IllegalArgumentException("date range bounds are required");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from must be on or before to");
        }
    }
}
