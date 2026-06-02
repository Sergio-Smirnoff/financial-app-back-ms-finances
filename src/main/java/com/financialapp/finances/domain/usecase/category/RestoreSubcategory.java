package com.financialapp.finances.domain.usecase.category;

import com.financialapp.finances.domain.model.category.Category;
import com.financialapp.finances.domain.usecase.category.command.RestoreSubcategoryCommand;

/** Restore (un-archive) a subcategory within one of the user's categories. Returns the aggregate. */
public interface RestoreSubcategory {
    Category execute(RestoreSubcategoryCommand command);
}
