package com.financialapp.finances.domain.model.category;

import com.financialapp.finances.domain.exception.category.InvalidCategoryNameException;

/**
 * The name of a {@link Category} or {@link Subcategory}: a non-blank, trimmed string of at most
 * 100 characters. Reifies what was a bare {@code String} so the rule lives in one place — this
 * record's compact constructor — and names gain value-equality. Specialised name VOs live with
 * their aggregate (cf. the banks {@code BankName}).
 */
public record CategoryName(String text) {

    private static final int MAX_NAME_LENGTH = 100;

    public CategoryName {
        if (text == null || text.isBlank()) {
            throw new InvalidCategoryNameException(text);
        }
        text = text.trim();
        if (text.length() > MAX_NAME_LENGTH) {
            throw new InvalidCategoryNameException(text);
        }
    }
}
