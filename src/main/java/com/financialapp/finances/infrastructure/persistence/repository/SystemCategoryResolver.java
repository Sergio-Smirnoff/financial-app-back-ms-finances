package com.financialapp.finances.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Resolves the id of the system 'Unassigned' category for a given type ('EXPENSE'|'INCOME').
 * Uses {@link JdbcTemplate} so it needs no managed entity for the legacy categories table — it
 * stays valid once the legacy JPA entities are removed.
 */
@Component
@RequiredArgsConstructor
public class SystemCategoryResolver {

    private static final String QUERY =
            "SELECT id FROM finances.categories " +
            "WHERE name = 'Unassigned' AND type = ? AND is_system = TRUE " +
            "ORDER BY id LIMIT 1";

    private final JdbcTemplate jdbcTemplate;

    public Optional<Long> findUnassignedCategoryId(String type) {
        List<Long> ids = jdbcTemplate.query(QUERY, (rs, rowNum) -> rs.getLong(1), type);
        return ids.stream().findFirst();
    }
}
