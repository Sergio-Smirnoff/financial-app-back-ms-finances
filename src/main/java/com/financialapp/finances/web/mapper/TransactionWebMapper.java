package com.financialapp.finances.web.mapper;

import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.model.transaction.ClassifiedTransaction;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.usecase.transaction.AccountTransactionView;
import com.financialapp.finances.web.dto.response.AccountTransactionResponse;
import com.financialapp.finances.web.dto.response.TransactionResponse;
import org.springframework.stereotype.Component;

@Component
public class TransactionWebMapper {

    /** User view: magnitude amount + reified kind. */
    public TransactionResponse toUserResponse(ClassifiedTransaction ct) {
        Transaction t = ct.transaction();
        return new TransactionResponse(
                t.id().value(), t.userId().value(),
                t.fromCbu().cbuNumber(), t.toCbu().cbuNumber(),
                t.money().amount(), t.money().currency().getCurrencyCode(),
                ct.kind(), t.categoryId().value(), t.description(), t.date());
    }

    /** Account view (ms-banks): amount signed for the queried account + resolved category names. */
    public AccountTransactionResponse toAccountResponse(AccountTransactionView v, Cbu accountCbu) {
        Transaction t = v.transaction();
        return new AccountTransactionResponse(
                t.id().value(), accountCbu.cbuNumber(),
                t.signedFor(accountCbu), t.money().currency().getCurrencyCode(),
                t.description(), v.names().category(), v.names().subcategory(), t.date());
    }
}
