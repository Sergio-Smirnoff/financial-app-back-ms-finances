package com.financialapp.finances.domain.model.transaction;

import com.financialapp.finances.domain.common.model.Cbu;
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

/**
 * A money movement from one account to another. Both sides are always a {@link Cbu}; whether
 * the transaction is an expense, income or transfer is derived per user from account ownership
 * (see {@code TransactionPosting}), never stored as a type or carried as a sign on {@link Money}.
 *
 * Immutable: every mutator returns a new instance and the single private constructor is the only
 * validation point. {@code id} is {@code null} until the transaction is persisted.
 */
public final class Transaction {

    private static final int MAX_DESCRIPTION_LENGTH = 500;

    private final TransactionId id;
    private final UserId userId;
    private final Cbu fromCbu;
    private final Cbu toCbu;
    private final Money money;
    private final CategoryId categoryId;
    private final String description;
    private final LocalDate date;

    /**
     * Events this aggregate has recorded but the application has not yet drained. The value fields
     * above stay immutable; only this buffer mutates (a standard aggregate event-recording seam).
     */
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Transaction(TransactionId id, UserId userId, Cbu fromCbu, Cbu toCbu, Money money,
                        CategoryId categoryId, String description, LocalDate date) {
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
        this.id = id;
    }

    /** New, unpersisted transaction (id == null). */
    public static Transaction create(UserId userId, Cbu fromCbu, Cbu toCbu, Money money,
                                     CategoryId categoryId, String description, LocalDate date) {
        return new Transaction(null, userId, fromCbu, toCbu, money, categoryId, description, date);
    }

    /** Rebuild from persistence (id required). */
    public static Transaction reconstitute(TransactionId id, UserId userId, Cbu fromCbu, Cbu toCbu,
                                           Money money, CategoryId categoryId, String description,
                                           LocalDate date) {
        return new Transaction(Objects.requireNonNull(id, "id must not be null"),
                userId, fromCbu, toCbu, money, categoryId, description, date);
    }

    /** Edit money/category/description/date; identity and the two accounts are frozen. */
    public Transaction changeDetails(Money money, CategoryId categoryId, String description,
                                     LocalDate date) {
        return new Transaction(id, userId, fromCbu, toCbu, money, categoryId, description, date);
    }

    /**
     * Signed amount of this transaction from one account's perspective: positive when {@code cbu}
     * is the destination (credit), negative when it is the source (debit). {@code cbu} must be one
     * of the two sides — otherwise it is a programming error.
     */
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

    /**
     * Record one {@link TransactionCreated} event per balance movement this transaction caused on an
     * owned account (computed by {@code TransactionPosting}). The aggregate must be persisted first:
     * each event carries this transaction's id, which ms-banks-bound consumers rely on. The
     * application drains them via {@link #pullDomainEvents()} and hands them to the event publisher.
     */
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

    /**
     * Record the undo of every balance movement this transaction caused (delete path). One
     * {@link TransactionReversed} per movement, carrying the negated amount.
     */
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

    /**
     * Record a money correction (edit path): undo the old movements then apply the new ones, so
     * ms-banks nets the delta. {@link TransactionReversed} per old movement, {@link TransactionCreated}
     * per new movement.
     */
    public void recordCorrection(List<BalanceMovement> oldMovements, List<BalanceMovement> newMovements) {
        recordReversal(oldMovements);
        recordCreationEvents(newMovements);
    }

    /** Return and clear the recorded domain events. */
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

    public TransactionId id() { return id; }
    public UserId userId() { return userId; }
    public Cbu fromCbu() { return fromCbu; }
    public Cbu toCbu() { return toCbu; }
    public Money money() { return money; }
    public CategoryId categoryId() { return categoryId; }
    public String description() { return description; }
    public LocalDate date() { return date; }
}
