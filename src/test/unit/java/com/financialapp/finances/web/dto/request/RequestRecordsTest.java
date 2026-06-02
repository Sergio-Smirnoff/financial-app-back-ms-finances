package com.financialapp.finances.web.dto.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class RequestRecordsTest {

    @Test void createSubcategoryRequest_exposesName() {
        assertThat(new CreateSubcategoryRequest("Dining").name()).isEqualTo("Dining");
    }

    @Test void updateCategoryRequest_exposesName() {
        assertThat(new UpdateCategoryRequest("Renamed").name()).isEqualTo("Renamed");
    }

    @Test void updateTransactionRequest_exposesAccessors() {
        UpdateTransactionRequest req = new UpdateTransactionRequest(5L, "desc", LocalDate.of(2026, 6, 1));
        assertThat(req.categoryId()).isEqualTo(5L);
        assertThat(req.description()).isEqualTo("desc");
        assertThat(req.date()).isEqualTo(LocalDate.of(2026, 6, 1));
    }

    @ParameterizedTest(name = "cat={0} desc={1} date={2} -> present={3}")
    @CsvSource({
            "true,  false, false, true",   // only categoryId
            "false, true,  false, true",   // only description
            "false, false, true,  true",   // only date
            "false, false, false, false",  // none -> rejected
    })
    void updateTransactionRequest_atLeastOneFieldPresent(boolean cat, boolean desc, boolean date, boolean expected) {
        UpdateTransactionRequest req = new UpdateTransactionRequest(
                cat ? 5L : null,
                desc ? "x" : null,
                date ? LocalDate.of(2026, 6, 1) : null);
        assertThat(req.isAtLeastOneFieldPresent()).isEqualTo(expected);
    }
}
