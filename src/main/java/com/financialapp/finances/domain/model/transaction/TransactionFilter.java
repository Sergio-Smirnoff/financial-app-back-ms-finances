package com.financialapp.finances.domain.model.transaction;

import com.financialapp.finances.domain.common.model.DateRange;
import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.common.model.Money;
import com.financialapp.finances.domain.common.model.UserId;

import java.util.Set;

public record TransactionFilter(
        UserId userId,
        Set<Cbu> ownedAccounts,
        Cbu accountCbu,
        CategoryId categoryId,
        DateRange dateRange,
        TransactionKind kind,
        boolean onlyUncategorised,
        Money amountMin,
        Money amountMax
) {
    public TransactionFilter {
        ownedAccounts = ownedAccounts != null ? Set.copyOf(ownedAccounts) : Set.of();
    }
}
