package com.financialapp.finances.domain.model.transaction;

import com.financialapp.commons.core.domain.model.Cbu;
import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.Money;
import com.financialapp.finances.domain.common.model.TransactionId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.event.DomainEvent;
import com.financialapp.finances.domain.event.TransactionCreated;
import com.financialapp.finances.domain.event.TransactionReversed;
import com.financialapp.finances.domain.exception.transaction.SameAccountTransactionException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

public final class Transaction {

    private static final int MAX_DESCRIPTION_LENGTH = 500;
    private static final int MAX_NOTE_LENGTH = 500;

    private final TransactionId id;
    private final UserId userId;
    private final Cbu fromCbu;
    private final Cbu toCbu;
    private final Money money;
    private final CategoryId categoryId;
    private final String description;
    private final LocalDate date;
    private final PaymentMethod paymentMethod;
    private final String note;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Transaction(TransactionId id, UserId userId, Cbu fromCbu, Cbu toCbu, Money money,
                        CategoryId categoryId, String description, LocalDate date,
                        PaymentMethod paymentMethod, String note) {
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.fromCbu = Objects.requireNonNull(fromCbu, "fromCbu must not be null");
        this.toCbu = Objects.requireNonNull(toCbu, "toCbu must not be null");
        this.money = Objects.requireNonNull(money, "money must not be null");
        this.categoryId = Objects.requireNonNull(categoryId, "categoryId must not be null");
        this.date = Objects.requireNonNull(date, "date must not be null");
        if (fromCbu.equals(toCbu)) {
            throw new SameAccountTransactionException(fromCbu);
        }
        this.description = normaliseDescription(description);
        this.paymentMethod = paymentMethod != null ? paymentMethod : PaymentMethod.OTHER;
        this.note = normaliseNote(note);
        this.id = id;
    }

    public static Transaction create(UserId userId, Cbu fromCbu, Cbu toCbu, Money money,
                                     CategoryId categoryId, String description, LocalDate date) {
        return create(userId, fromCbu, toCbu, money, categoryId, description, date, PaymentMethod.OTHER, null);
    }

    public static Transaction create(UserId userId, Cbu fromCbu, Cbu toCbu, Money money,
                                     CategoryId categoryId, String description, LocalDate date,
                                     PaymentMethod paymentMethod, String note) {
        return new Transaction(null, userId, fromCbu, toCbu, money, categoryId, description, date, paymentMethod, note);
    }

    public static Transaction reconstitute(TransactionId id, UserId userId, Cbu fromCbu, Cbu toCbu,
                                           Money money, CategoryId categoryId, String description,
                                           LocalDate date) {
        return reconstitute(id, userId, fromCbu, toCbu, money, categoryId, description, date, PaymentMethod.OTHER, null);
    }

    public static Transaction reconstitute(TransactionId id, UserId userId, Cbu fromCbu, Cbu toCbu,
                                           Money money, CategoryId categoryId, String description,
                                           LocalDate date, PaymentMethod paymentMethod, String note) {
        return new Transaction(Objects.requireNonNull(id, "id must not be null"),
                userId, fromCbu, toCbu, money, categoryId, description, date, paymentMethod, note);
    }

    public Transaction changeDetails(CategoryId categoryId, String description, LocalDate date) {
        return changeDetails(categoryId, description, date, this.note);
    }

    public Transaction changeDetails(CategoryId categoryId, String description, LocalDate date, String note) {
        return new Transaction(id, userId, fromCbu, toCbu, money, categoryId, description, date, paymentMethod, note);
    }

    public BigDecimal signedFor(Cbu cbu) {
        if (toCbu.equals(cbu)) {
            return money.amount();
        }
        if (fromCbu.equals(cbu)) {
            return money.amount().negate();
        }
        throw new IllegalArgumentException(
            "CBU " + cbu.cbuNumber() + " is not part of this transaction");
    }

    public boolean involves(Cbu cbu) {
        return fromCbu.equals(cbu) || toCbu.equals(cbu);
    }

    public void recordCreationEvents(List<BalanceMovement> movements) {
        if (id == null) {
            throw new IllegalStateException(
                "cannot record creation events before the transaction is persisted");
        }
        for (BalanceMovement movement : movements) {
            domainEvents.add(new TransactionCreated(
                id, movement.account(), movement.signedAmount(), movement.currency()));
        }
    }

    public void recordReversal(List<BalanceMovement> movements) {
        if (id == null) {
            throw new IllegalStateException(
                "cannot record a reversal before the transaction is persisted");
        }
        for (BalanceMovement movement : movements) {
            BalanceMovement undo = movement.reversed();
            domainEvents.add(new TransactionReversed(
                id, undo.account(), undo.signedAmount(), undo.currency()));
        }
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> drained = List.copyOf(domainEvents);
        domainEvents.clear();
        return drained;
    }

    public Currency currency() {
        return money.currency();
    }

    private static String normaliseDescription(String description) {
        if (description == null) {
            return null;
        }
        String trimmed = description.trim();
        if (trimmed.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException(
                "description must be at most " + MAX_DESCRIPTION_LENGTH + " characters");
        }
        return trimmed;
    }

    private static String normaliseNote(String note) {
        if (note == null) {
            return null;
        }
        String trimmed = note.trim();
        if (trimmed.length() > MAX_NOTE_LENGTH) {
            throw new IllegalArgumentException(
                "note must be at most " + MAX_NOTE_LENGTH + " characters");
        }
        return trimmed;
    }

    public TransactionId id() { return id; }
    public UserId userId() { return userId; }
    public Cbu fromCbu() { return fromCbu; }
    public Cbu toCbu() { return toCbu; }
    public Money money() { return money; }
    public CategoryId categoryId() { return categoryId; }
    public String description() { return description; }
    public LocalDate date() { return date; }
    public PaymentMethod paymentMethod() { return paymentMethod; }
    public String note() { return note; }
}
