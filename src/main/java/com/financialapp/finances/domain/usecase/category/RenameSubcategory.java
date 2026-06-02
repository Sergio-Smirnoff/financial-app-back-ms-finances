package com.financialapp.finances.domain.usecase.category;

import com.financialapp.finances.domain.model.category.Category;
import com.financialapp.finances.domain.usecase.category.command.RenameSubcategoryCommand;

/** Rename a subcategory within one of the user's categories. Returns the aggregate. */
public interface RenameSubcategory {
    Category execute(RenameSubcategoryCommand command);
}
