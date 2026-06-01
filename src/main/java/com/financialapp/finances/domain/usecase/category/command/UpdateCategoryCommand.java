package com.financialapp.finances.domain.usecase.category.command;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.category.CategoryName;

public record UpdateCategoryCommand(UserId userId, CategoryId categoryId, CategoryName newName) {
}
