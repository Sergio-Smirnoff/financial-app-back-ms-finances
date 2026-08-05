package com.financialapp.finances.infrastructure.persistence.mapper;

import com.financialapp.commons.core.domain.model.Cbu;

import com.financialapp.finances.domain.common.model.*;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.infrastructure.persistence.entity.TransactionJpaEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Currency;

@Component
public class TransactionPersistenceMapper {

    public TransactionJpaEntity toEntity(Transaction t) {
        LocalDateTime now = LocalDateTime.now();
        return TransactionJpaEntity.builder()
                .id(t.id() == null ? null : t.id().value())
                .userId(t.userId().value())
                .fromCbu(t.fromCbu().cbuNumber())
                .toCbu(t.toCbu().cbuNumber())
                .amount(t.money().amount())
                .currency(t.money().currency().getCurrencyCode())
                .categoryId(t.categoryId().value())
                .description(t.description())
                .date(t.date())
                .paymentMethod(t.paymentMethod() != null ? t.paymentMethod().name() : null)
                .note(t.note())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public Transaction toDomain(TransactionJpaEntity e) {
        com.financialapp.finances.domain.model.transaction.PaymentMethod pm = e.getPaymentMethod() != null
                ? com.financialapp.finances.domain.model.transaction.PaymentMethod.valueOf(e.getPaymentMethod())
                : com.financialapp.finances.domain.model.transaction.PaymentMethod.OTHER;
        return Transaction.reconstitute(
                new TransactionId(e.getId()),
                new UserId(e.getUserId()),
                new Cbu(e.getFromCbu()),
                new Cbu(e.getToCbu()),
                new Money(e.getAmount(), Currency.getInstance(e.getCurrency())),
                new CategoryId(e.getCategoryId()),
                e.getDescription(),
                e.getDate(),
                pm,
                e.getNote());
    }
}
