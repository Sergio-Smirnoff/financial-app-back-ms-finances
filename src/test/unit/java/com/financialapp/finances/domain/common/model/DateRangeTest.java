package com.financialapp.finances.domain.common.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DateRangeTest {
    @Test void accepts_from_before_or_equal_to() {
        DateRange r = new DateRange(LocalDate.parse("2026-01-01"), LocalDate.parse("2026-12-31"));
        assertEquals(LocalDate.parse("2026-01-01"), r.from());
        assertEquals(LocalDate.parse("2026-12-31"), r.to());
        assertDoesNotThrow(() -> new DateRange(LocalDate.parse("2026-05-05"), LocalDate.parse("2026-05-05")));
    }

    @Test void rejects_null_bounds_and_inverted_range() {
        assertThrows(IllegalArgumentException.class, () -> new DateRange(null, LocalDate.now()));
        assertThrows(IllegalArgumentException.class, () -> new DateRange(LocalDate.now(), null));
        assertThrows(IllegalArgumentException.class,
                () -> new DateRange(LocalDate.parse("2026-12-31"), LocalDate.parse("2026-01-01")));
    }
}
