package com.financialapp.finances.domain.model.category;

import com.financialapp.finances.domain.exception.category.InvalidCategoryNameException;

/**
 * The name of a {@link Category} or {@link Subcategory}: a non-blank, trimmed string of at most
 * 100 characters. Reifies what was a bare {@code String} so the rule lives in one place — this
 * record's compact constructor — and names gain value-equality. Specialised name VOs live with
 * their aggregate (cf. the banks {@code BankName}).
 */
public record CategoryName(String value) {

    private static final int MAX_LENGTH = 100;

    public CategoryName {
        if (value == null || value.isBlank()) {
            throw new InvalidCategoryNameException(value);
        }
        value = value.trim();
        if (value.length() > MAX_LENGTH) {
            throw new InvalidCategoryNameException(value);
        }
    }
}
