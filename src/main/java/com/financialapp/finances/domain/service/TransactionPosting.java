package com.financialapp.finances.domain.service;

import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.exception.transaction.UnownedTransactionException;
import com.financialapp.finances.domain.model.transaction.BalanceMovement;
import com.financialapp.finances.domain.model.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Pure domain service: derives the balance movements a transaction must cause, given the set of
 * accounts the user owns. I/O-free — the application resolves the owned-set via
 * {@code AccountOwnershipGateway} and passes it in. One movement per owned side (expense/income → 1,
 * transfer → 2). Throws if the transaction touches none of the user's accounts (it cannot belong to
 * their ledger). The expense/income/transfer label is not produced here — it is inferred by the
 * read side, from the same owned-set, when a consumer needs the label.
 */
public final class TransactionPosting {

    public List<BalanceMovement> post(Transaction transaction, Set<Cbu> ownedAccounts) {
        boolean ownsFrom = ownedAccounts.contains(transaction.fromCbu());
        boolean ownsTo = ownedAccounts.contains(transaction.toCbu());

        if (!ownsFrom && !ownsTo) {
            throw new UnownedTransactionException(
                transaction.userId(), transaction.fromCbu(), transaction.toCbu());
        }

        List<BalanceMovement> movements = new ArrayList<>(2);
        if (ownsFrom) {
            movements.add(new BalanceMovement(
                transaction.fromCbu(), transaction.signedFor(transaction.fromCbu()),
                transaction.currency()));
        }
        if (ownsTo) {
            movements.add(new BalanceMovement(
                transaction.toCbu(), transaction.signedFor(transaction.toCbu()),
                transaction.currency()));
        }
        return List.copyOf(movements);
    }
}
