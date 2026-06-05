package com.financialapp.finances.domain.exception.category;

import com.financialapp.finances.domain.exception.DomainErrorCode;
import com.financialapp.commons.core.error.DomainException;

import java.util.Map;

/** A category or subcategory name was blank or longer than 100 characters. */
public class InvalidCategoryNameException extends DomainException {
    public InvalidCategoryNameException(String name) {
        super(DomainErrorCode.INVALID_CATEGORY_NAME,
              "Invalid category name: '" + name + "'. Must be non-blank and at most 100 characters.",
              Map.of("name", String.valueOf(name)));
    }
}
