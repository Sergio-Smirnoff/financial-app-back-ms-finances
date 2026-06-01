package com.financialapp.finances.domain.model.transaction;

/** A transaction paired with its owned-set-derived kind, for read consumers. */
public record ClassifiedTransaction(Transaction transaction, TransactionKind kind) {}
