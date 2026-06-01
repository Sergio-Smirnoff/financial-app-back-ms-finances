package com.financialapp.finances.domain.usecase.transaction;

import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.model.transaction.Transaction;

import java.time.LocalDate;
import java.util.List;

/** Transactions touching one account (ms-banks callback; the web mapper signs them per account). */
public interface ListAccountTransactions {
    List<Transaction> execute(Cbu accountCbu, Integer limit, LocalDate from, LocalDate to);
}
