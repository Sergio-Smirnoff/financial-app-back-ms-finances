package com.financialapp.finances.domain.usecase.category;

import com.financialapp.finances.domain.model.category.Category;
import com.financialapp.finances.domain.usecase.category.command.ArchiveCategoryCommand;

/** Archive (soft-delete) one of the user's categories. Returns the updated aggregate. */
public interface ArchiveCategory {
    Category execute(ArchiveCategoryCommand command);
}
