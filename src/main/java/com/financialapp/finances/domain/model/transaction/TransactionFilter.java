package com.financialapp.finances.domain.model.transaction;

import com.financialapp.finances.domain.common.model.DateRange;
import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.commons.core.domain.model.Cbu;
import com.financialapp.finances.domain.common.model.Money;
import com.financialapp.finances.domain.common.model.UserId;

import java.util.List;
import java.util.Set;

public record TransactionFilter(
        UserId userId,
        Set<Cbu> ownedAccounts,
        List<Cbu> accountCbus,
        List<CategoryId> categoryIds,
        DateRange dateRange,
        TransactionKind kind,
        boolean onlyUncategorised,
        Money amountMin,
        Money amountMax,
        PaymentMethod paymentMethod,
        DescriptionQuery descriptionQuery) {

    public TransactionFilter {
        ownedAccounts = ownedAccounts != null ? Set.copyOf(ownedAccounts) : Set.of();
        accountCbus = accountCbus != null ? List.copyOf(accountCbus) : List.of();
        categoryIds = categoryIds != null ? List.copyOf(categoryIds) : List.of();
    }
}
