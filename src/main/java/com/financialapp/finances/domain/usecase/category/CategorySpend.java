package com.financialapp.finances.domain.usecase.category;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.Money;

public record CategorySpend(CategoryId categoryId, String categoryName, Money total, long transactionCount) {
}
