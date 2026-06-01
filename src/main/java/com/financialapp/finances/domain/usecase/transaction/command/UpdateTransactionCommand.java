package com.financialapp.finances.domain.usecase.transaction.command;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.Money;
import com.financialapp.finances.domain.common.model.TransactionId;
import com.financialapp.finances.domain.common.model.UserId;

import java.time.LocalDate;

/** Edit the money/category/description/date of an existing transaction; the accounts are frozen. */
public record UpdateTransactionCommand(
        UserId userId,
        TransactionId id,
        Money money,
        CategoryId categoryId,
        String description,
        LocalDate date) {}
