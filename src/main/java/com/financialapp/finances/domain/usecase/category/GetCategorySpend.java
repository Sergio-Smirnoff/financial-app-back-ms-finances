package com.financialapp.finances.domain.usecase.category;

import com.financialapp.finances.domain.common.model.DateRange;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.transaction.TransactionKind;

import java.util.List;

public interface GetCategorySpend {
    List<CategorySpend> execute(UserId userId, DateRange dateRange, TransactionKind kind);
}
