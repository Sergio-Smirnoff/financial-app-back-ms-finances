package com.financialapp.finances.infrastructure.persistence.repository;

import com.financialapp.finances.application.transaction.CategoryNameLookup;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * JDBC adapter for {@link CategoryNameLookup}: resolves a category id to display names from the
 * legacy categories table (subcategory name + its parent category name). Temporary bridge until
 * the Category aggregate migrates (slice 5).
 */
@Component
@RequiredArgsConstructor
public class CategoryNameResolver implements CategoryNameLookup {

    private static final String QUERY =
            "SELECT c.name AS name, p.name AS parent_name " +
            "FROM finances.categories c " +
            "LEFT JOIN finances.categories p ON c.parent_id = p.id " +
            "WHERE c.id = ?";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public CategoryNames resolve(Long categoryId) {
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(QUERY, categoryId);
            String name = (String) row.get("name");
            String parent = (String) row.get("parent_name");
            // If the id is a parent category itself, parent_name is null → category=name, no subcategory.
            return parent == null
                    ? new CategoryNames(name, null)
                    : new CategoryNames(parent, name);
        } catch (EmptyResultDataAccessException ex) {
            return new CategoryNames(null, null);
        }
    }
}
