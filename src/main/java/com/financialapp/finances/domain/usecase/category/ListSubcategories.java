package com.financialapp.finances.domain.usecase.category;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.category.Subcategory;

import java.util.List;

/** The ACTIVE subcategories of one of the user's categories. */
public interface ListSubcategories {
    List<Subcategory> execute(CategoryId categoryId, UserId userId);
}
