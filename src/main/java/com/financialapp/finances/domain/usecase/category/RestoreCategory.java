package com.financialapp.finances.domain.usecase.category;

import com.financialapp.finances.domain.model.category.Category;
import com.financialapp.finances.domain.usecase.category.command.RestoreCategoryCommand;

/** Restore a previously archived category to ACTIVE. Returns the updated aggregate. */
public interface RestoreCategory {
    Category execute(RestoreCategoryCommand command);
}
