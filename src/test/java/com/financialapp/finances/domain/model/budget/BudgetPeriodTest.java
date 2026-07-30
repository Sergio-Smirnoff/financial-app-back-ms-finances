package com.financialapp.finances.domain.model.budget;

import com.financialapp.finances.domain.common.model.DateRange;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BudgetPeriodTest {

    @Test
    void exposesItsCalendarMonthAsDateRange() {
        DateRange range = new BudgetPeriod(2026, 7).dateRange();
        assertThat(range.from()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(range.to()).isEqualTo(LocalDate.of(2026, 7, 31));
    }

    @Test
    void navigatesAcrossYearBoundaries() {
        assertThat(new BudgetPeriod(2026, 1).previous()).isEqualTo(new BudgetPeriod(2025, 12));
        assertThat(new BudgetPeriod(2026, 12).next()).isEqualTo(new BudgetPeriod(2027, 1));
    }

    @Test
    void buildsFromAnyDateInsideTheMonth() {
        assertThat(BudgetPeriod.from(LocalDate.of(2026, 7, 29))).isEqualTo(new BudgetPeriod(2026, 7));
    }

    @Test
    void rejectsInvalidMonths() {
        assertThatThrownBy(() -> new BudgetPeriod(2026, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new BudgetPeriod(2026, 13)).isInstanceOf(IllegalArgumentException.class);
    }
}
