package com.financialapp.finances.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Resolves the id of the single system 'Unassigned' category that the ledger stamps on
 * auto-created transactions. Uses {@link JdbcTemplate} so it needs no managed entity for the
 * categories table. Since slice 5 there is one Unassigned category (no per-direction split).
 */
@Component
@RequiredArgsConstructor
public class SystemCategoryResolver {

    private static final String QUERY =
            "SELECT id FROM finances.categories " +
            "WHERE name = 'Unassigned' AND is_system = TRUE " +
            "ORDER BY id LIMIT 1";

    private final JdbcTemplate jdbcTemplate;

    public Optional<Long> findUnassignedCategoryId() {
        List<Long> ids = jdbcTemplate.query(QUERY, (rs, rowNum) -> rs.getLong(1));
        return ids.stream().findFirst();
    }
}
