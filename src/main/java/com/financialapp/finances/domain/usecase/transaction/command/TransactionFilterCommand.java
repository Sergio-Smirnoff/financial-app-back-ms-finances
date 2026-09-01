package com.financialapp.finances.domain.usecase.transaction.command;

import com.financialapp.finances.domain.common.model.DateRange;
import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.commons.core.domain.model.Cbu;
import com.financialapp.finances.domain.common.model.Money;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.transaction.CursorPage;
import com.financialapp.finances.domain.model.transaction.TransactionKind;

public record TransactionFilterCommand(
        UserId userId,
        Cbu accountCbu,
        CategoryId categoryId,
        DateRange dateRange,
        TransactionKind kind,
        boolean onlyUncategorised,
        Money amountMin,
        Money amountMax,
        CursorPage page
) { }
