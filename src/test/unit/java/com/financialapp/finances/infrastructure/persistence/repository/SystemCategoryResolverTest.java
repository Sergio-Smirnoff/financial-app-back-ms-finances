package com.financialapp.finances.infrastructure.persistence.repository;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SystemCategoryResolverTest {

    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final SystemCategoryResolver resolver = new SystemCategoryResolver(jdbc);

    @SuppressWarnings("unchecked")
    @Test void findsUnassignedId_whenRowPresent() {
        // Given the system 'Unassigned' category row exists
        when(jdbc.query(anyString(), any(RowMapper.class))).thenReturn(List.of(900L));
        // When resolved / Then its id is returned
        assertThat(resolver.findUnassignedCategoryId()).contains(900L);
    }

    @SuppressWarnings("unchecked")
    @Test void empty_whenNoRow() {
        // Given no matching row
        when(jdbc.query(anyString(), any(RowMapper.class))).thenReturn(List.of());
        // When resolved / Then empty
        assertThat(resolver.findUnassignedCategoryId()).isEmpty();
    }
}
