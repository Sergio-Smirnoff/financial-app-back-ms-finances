package com.financialapp.finances.domain.usecase.transaction;

import com.financialapp.finances.domain.common.model.DateRange;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.transaction.MonthlyFlow;

import java.util.List;

/** Contiguous monthly income and expense flow series for a user over a date range. */
public interface GetMonthlyFlow {
    List<MonthlyFlow> execute(UserId userId, DateRange range);
}
