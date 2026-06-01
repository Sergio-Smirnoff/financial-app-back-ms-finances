package com.financialapp.finances.domain.usecase.category.command;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.UserId;

public record ArchiveSubcategoryCommand(UserId userId, CategoryId categoryId, CategoryId subId) {
}
