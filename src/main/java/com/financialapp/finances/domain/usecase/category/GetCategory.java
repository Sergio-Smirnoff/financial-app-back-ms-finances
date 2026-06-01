package com.financialapp.finances.domain.usecase.category;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.category.Category;

/** One of the user's categories by id (any status). */
public interface GetCategory {
    Category execute(CategoryId categoryId, UserId userId);
}
