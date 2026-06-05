package com.financialapp.finances.domain.exception.category;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.exception.DomainErrorCode;
import com.financialapp.commons.core.error.DomainException;

import java.util.Map;

/** A subcategory operation referenced an id that is not part of the target Category aggregate. */
public class SubcategoryNotInCategoryException extends DomainException {
    public SubcategoryNotInCategoryException(CategoryId categoryId, CategoryId subId) {
        super(DomainErrorCode.SUBCATEGORY_NOT_IN_CATEGORY,
              "Subcategory " + idValue(subId) + " does not belong to category " + idValue(categoryId) + ".",
              Map.of("categoryId", idValue(categoryId), "subId", idValue(subId)));
    }

    private static String idValue(CategoryId id) {
        return id == null ? "null" : String.valueOf(id.value());
    }
}
