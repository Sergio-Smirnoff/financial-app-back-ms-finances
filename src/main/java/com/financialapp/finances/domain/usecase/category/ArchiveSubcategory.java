package com.financialapp.finances.domain.usecase.category;

import com.financialapp.finances.domain.model.category.Category;
import com.financialapp.finances.domain.usecase.category.command.ArchiveSubcategoryCommand;

/** Archive (soft-delete) a subcategory within one of the user's categories. Returns the aggregate. */
public interface ArchiveSubcategory {
    Category execute(ArchiveSubcategoryCommand command);
}
