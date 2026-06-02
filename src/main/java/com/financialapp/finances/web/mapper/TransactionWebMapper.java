package com.financialapp.finances.web.mapper;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.common.model.Money;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.category.CategoryNames;
import com.financialapp.finances.domain.model.transaction.ClassifiedTransaction;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.usecase.transaction.AccountTransactionView;
import com.financialapp.finances.domain.usecase.transaction.UserTransactionView;
import com.financialapp.finances.domain.usecase.transaction.command.RecordTransactionCommand;
import com.financialapp.finances.web.dto.request.RecordTransactionRequest;
import com.financialapp.finances.web.dto.response.AccountTransactionResponse;
import com.financialapp.finances.web.dto.response.TransactionResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Currency;

@Component
public class TransactionWebMapper {

    /** Inbound: parse the request (money as decimal String) into the domain command. */
    public RecordTransactionCommand toRecordCommand(UserId userId, RecordTransactionRequest req) {
        return new RecordTransactionCommand(
                userId, new Cbu(req.fromCbu()), new Cbu(req.toCbu()),
                new Money(new BigDecimal(req.amount()), Currency.getInstance(req.currency())),
                new CategoryId(req.categoryId()), req.description(), req.date());
    }

    /** User view with resolved category display name (list path). */
    public TransactionResponse toUserResponse(UserTransactionView v) {
        CategoryNames names = v.names();
        String displayName = names.subcategory() != null ? names.subcategory() : names.category();
        return toUserResponse(v.classified(), displayName);
    }

    /** User view: magnitude amount + reified kind. categoryName may be null when unresolved. */
    public TransactionResponse toUserResponse(ClassifiedTransaction ct, String categoryName) {
        Transaction t = ct.transaction();
        return new TransactionResponse(
                t.id().value(), t.userId().value(),
                t.fromCbu().cbuNumber(), t.toCbu().cbuNumber(),
                t.money().amount().toPlainString(), t.money().currency().getCurrencyCode(),
                ct.kind(), t.categoryId().value(), categoryName, t.description(), t.date());
    }

    /** Account view (ms-banks): amount signed for the queried account + resolved category names. */
    public AccountTransactionResponse toAccountResponse(AccountTransactionView v, Cbu accountCbu) {
        Transaction t = v.transaction();
        return new AccountTransactionResponse(
                t.id().value(), accountCbu.cbuNumber(),
                t.signedFor(accountCbu).toPlainString(), t.money().currency().getCurrencyCode(),
                t.description(), v.names().category(), v.names().subcategory(), t.date());
    }
}
