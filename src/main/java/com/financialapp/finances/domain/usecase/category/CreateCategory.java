package com.financialapp.finances.domain.usecase.category;

import com.financialapp.finances.domain.model.category.Category;
import com.financialapp.finances.domain.usecase.category.command.CreateCategoryCommand;

/** Create a new category for a user. Implemented in the application layer (later slice). */
public interface CreateCategory {
    Category execute(CreateCategoryCommand command);
}
