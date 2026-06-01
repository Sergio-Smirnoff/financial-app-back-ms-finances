package com.financialapp.finances.domain.usecase.category;

import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.category.Category;

import java.util.List;

/** A user's ACTIVE categories. */
public interface ListCategories {
    List<Category> execute(UserId userId);
}
