package com.financialapp.finances.web.mapper;

import com.financialapp.finances.application.transaction.CategoryNameLookup;
import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.model.transaction.ClassifiedTransaction;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.web.dto.response.AccountTransactionResponse;
import com.financialapp.finances.web.dto.response.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionWebMapper {

    private final CategoryNameLookup categoryNames;

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
    public AccountTransactionResponse toAccountResponse(Transaction t, Cbu accountCbu) {
        CategoryNameLookup.CategoryNames names = categoryNames.resolve(t.categoryId().value());
        return new AccountTransactionResponse(
                t.id().value(), accountCbu.cbuNumber(),
                t.signedFor(accountCbu), t.money().currency().getCurrencyCode(),
                t.description(), names.category(), names.subcategory(), t.date());
    }
}
