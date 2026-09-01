package com.financialapp.finances.web.mapper;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.commons.core.domain.model.Cbu;
import com.financialapp.finances.domain.common.model.Money;
import com.financialapp.finances.domain.common.model.TransactionId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.category.CategoryNames;
import com.financialapp.finances.domain.model.transaction.ClassifiedTransaction;
import com.financialapp.finances.domain.model.transaction.PaymentMethod;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.usecase.transaction.AccountTransactionView;
import com.financialapp.finances.domain.usecase.transaction.UserTransactionView;
import com.financialapp.finances.domain.usecase.transaction.command.RecordTransactionCommand;
import com.financialapp.finances.domain.usecase.transaction.command.UpdateTransactionCommand;
import com.financialapp.finances.web.dto.request.RecordTransactionRequest;
import com.financialapp.finances.web.dto.request.UpdateTransactionRequest;
import com.financialapp.finances.domain.model.transaction.MonthlyFlow;
import com.financialapp.finances.web.dto.response.AccountTransactionResponse;
import com.financialapp.finances.web.dto.response.MonthlyFlowResponse;
import com.financialapp.finances.web.dto.response.TransactionResponse;
import com.financialapp.finances.web.dto.response.TransactionSearchResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

@Component
public class TransactionWebMapper {

    /** Inbound: parse the request (money as decimal String) into the domain command. */
    public RecordTransactionCommand toRecordCommand(UserId userId, RecordTransactionRequest req) {
        PaymentMethod pm = req.paymentMethod() != null && !req.paymentMethod().isBlank()
                ? PaymentMethod.valueOf(req.paymentMethod())
                : PaymentMethod.OTHER;
        return new RecordTransactionCommand(
                userId, new Cbu(req.fromCbu()), new Cbu(req.toCbu()),
                new Money(new BigDecimal(req.amount()), Currency.getInstance(req.currency())),
                new CategoryId(req.categoryId()), req.description(), req.date(),
                pm, req.note());
    }

    public UpdateTransactionCommand toUpdateCommand(UserId userId, Long id, UpdateTransactionRequest req) {
        return new UpdateTransactionCommand(
                userId, new TransactionId(id),
                req.categoryId() != null ? new CategoryId(req.categoryId()) : null,
                req.description(), req.date(), req.note());
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
                ct.kind(), t.categoryId().value(), categoryName, t.description(), t.date(),
                t.paymentMethod(), t.note());
    }

    /** Account view (ms-banks): amount signed for the queried account + resolved category names. */
    public AccountTransactionResponse toAccountResponse(AccountTransactionView v, Cbu accountCbu) {
        Transaction t = v.transaction();
        return new AccountTransactionResponse(
                t.id().value(), accountCbu.cbuNumber(),
                t.signedFor(accountCbu).toPlainString(), t.money().currency().getCurrencyCode(),
                t.description(), v.names().category(), v.names().subcategory(), t.date());
    }

    public TransactionSearchResponse toSearchResponse(ClassifiedTransaction ct) {
        Transaction t = ct.transaction();
        String direction = switch (ct.kind()) {
            case EXPENSE -> "OUT";
            case INCOME -> "IN";
            case TRANSFER -> "TRANSFER";
        };
        return new TransactionSearchResponse(
                t.id().value(),
                t.date(),
                t.description(),
                t.money().amount().toPlainString(),
                t.money().currency().getCurrencyCode(),
                direction
        );
    }

    public List<TransactionSearchResponse> toSearchResponses(List<ClassifiedTransaction> classifiedList) {
        return classifiedList.stream().map(this::toSearchResponse).toList();
    }

    public MonthlyFlowResponse toMonthlyFlowResponse(MonthlyFlow flow) {
        return new MonthlyFlowResponse(
                flow.month().toString(),
                flow.currency().getCurrencyCode(),
                flow.income().toPlainString(),
                flow.expense().toPlainString()
        );
    }

    public List<MonthlyFlowResponse> toMonthlyFlowResponses(List<MonthlyFlow> flows) {
        return flows.stream().map(this::toMonthlyFlowResponse).toList();
    }
}
