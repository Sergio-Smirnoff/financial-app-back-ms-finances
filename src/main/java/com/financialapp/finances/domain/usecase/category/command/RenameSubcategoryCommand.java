package com.financialapp.finances.domain.usecase.category.command;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.category.CategoryName;

public record RenameSubcategoryCommand(UserId userId, CategoryId categoryId, CategoryId subId, CategoryName newName) {
}
