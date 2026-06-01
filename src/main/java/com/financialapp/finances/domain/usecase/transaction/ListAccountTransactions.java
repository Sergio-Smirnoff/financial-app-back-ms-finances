package com.financialapp.finances.domain.usecase.transaction;

import com.financialapp.finances.domain.common.model.Cbu;

import java.time.LocalDate;
import java.util.List;

/** Transactions touching one account (ms-banks callback), each paired with resolved category names. */
public interface ListAccountTransactions {
    List<AccountTransactionView> execute(Cbu accountCbu, Integer limit, LocalDate from, LocalDate to);
}
