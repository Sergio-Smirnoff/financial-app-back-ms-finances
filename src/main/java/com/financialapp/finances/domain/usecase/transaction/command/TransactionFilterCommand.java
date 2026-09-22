package com.financialapp.finances.domain.usecase.transaction.command;

import com.financialapp.finances.domain.common.model.DateRange;
import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.commons.core.domain.model.Cbu;
import com.financialapp.finances.domain.common.model.Money;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.transaction.CursorPage;
import com.financialapp.finances.domain.model.transaction.DescriptionQuery;
import com.financialapp.finances.domain.model.transaction.TransactionKind;

import com.financialapp.finances.domain.model.transaction.PaymentMethod;

import java.util.List;

public record TransactionFilterCommand(
        UserId userId,
        List<Cbu> accountCbus,
        List<CategoryId> categoryIds,
        DateRange dateRange,
        TransactionKind kind,
        boolean onlyUncategorised,
        Money amountMin,
        Money amountMax,
        PaymentMethod paymentMethod,
        DescriptionQuery descriptionQuery,
        CursorPage page) {

    public TransactionFilterCommand {
        accountCbus = accountCbus != null ? List.copyOf(accountCbus) : List.of();
        categoryIds = categoryIds != null ? List.copyOf(categoryIds) : List.of();
    }
}
