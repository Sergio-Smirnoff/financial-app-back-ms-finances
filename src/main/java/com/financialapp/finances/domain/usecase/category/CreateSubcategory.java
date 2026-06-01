package com.financialapp.finances.domain.usecase.category;

import com.financialapp.finances.domain.model.category.Category;
import com.financialapp.finances.domain.usecase.category.command.CreateSubcategoryCommand;

/** Add a subcategory to one of the user's categories. Returns the updated aggregate. */
public interface CreateSubcategory {
    Category execute(CreateSubcategoryCommand command);
}
