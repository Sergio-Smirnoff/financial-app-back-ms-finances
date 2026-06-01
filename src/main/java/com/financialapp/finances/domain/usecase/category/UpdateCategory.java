package com.financialapp.finances.domain.usecase.category;

import com.financialapp.finances.domain.model.category.Category;
import com.financialapp.finances.domain.usecase.category.command.UpdateCategoryCommand;

/** Rename one of the user's categories. Returns the updated aggregate. */
public interface UpdateCategory {
    Category execute(UpdateCategoryCommand command);
}
