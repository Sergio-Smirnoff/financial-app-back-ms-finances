package com.financialapp.finances.domain.model.transaction;

/**
 * The user-facing classification of a transaction, derived from which sides the user owns:
 * owns source only = EXPENSE, owns destination only = INCOME, owns both = TRANSFER.
 * Reified once here so no read consumer re-derives it ad hoc.
 */
public enum TransactionKind {
    EXPENSE, INCOME, TRANSFER
}
