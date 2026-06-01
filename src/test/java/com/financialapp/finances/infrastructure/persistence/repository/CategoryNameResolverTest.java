package com.financialapp.finances.infrastructure.persistence.repository;

import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CategoryNameResolverTest {

    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final CategoryNameResolver resolver = new CategoryNameResolver(jdbc);

    @Test
    void returnsNamesForKnownCategory() {
        when(jdbc.queryForMap(anyString(), eq(5L)))
                .thenReturn(Map.of("name", "Rent", "parent_name", "Housing"));
        CategoryNameResolver.CategoryNames names = resolver.resolve(5L);
        assertThat(names.category()).isEqualTo("Housing");
        assertThat(names.subcategory()).isEqualTo("Rent");
    }

    @Test
    void returnsNullsWhenAbsent() {
        when(jdbc.queryForMap(anyString(), eq(99L)))
                .thenThrow(new EmptyResultDataAccessException(1));
        CategoryNameResolver.CategoryNames names = resolver.resolve(99L);
        assertThat(names.category()).isNull();
        assertThat(names.subcategory()).isNull();
    }
}
