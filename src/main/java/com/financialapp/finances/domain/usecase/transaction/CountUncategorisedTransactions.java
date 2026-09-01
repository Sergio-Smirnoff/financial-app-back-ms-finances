package com.financialapp.finances.domain.usecase.transaction;

import com.financialapp.finances.domain.common.model.UserId;

public interface CountUncategorisedTransactions {
    long execute(UserId userId);
}
