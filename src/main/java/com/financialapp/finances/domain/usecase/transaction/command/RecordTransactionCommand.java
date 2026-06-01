package com.financialapp.finances.domain.usecase.transaction.command;

import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.Money;
import com.financialapp.finances.domain.common.model.UserId;

import java.time.LocalDate;

/** Record any transaction: expense, income or transfer — classification is derived from ownership. */
public record RecordTransactionCommand(
        UserId userId,
        Cbu fromCbu,
        Cbu toCbu,
        Money money,
        CategoryId categoryId,
        String description,
        LocalDate date) {}
