package com.financialapp.finances.domain.service;

import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.model.transaction.TransactionKind;

import java.util.Set;

/**
 * Single authority for the expense/income/transfer label, derived from which sides of a transaction
 * the user owns. Pure; the application supplies the owned-set (via AccountOwnershipGateway).
 */
public final class TransactionClassifier {

    public TransactionKind classify(Transaction transaction, Set<Cbu> ownedAccounts) {
        boolean ownsFrom = ownedAccounts.contains(transaction.fromCbu());
        boolean ownsTo = ownedAccounts.contains(transaction.toCbu());
        if (ownsFrom && ownsTo) {
            return TransactionKind.TRANSFER;
        }
        if (ownsFrom) {
            return TransactionKind.EXPENSE;
        }
        if (ownsTo) {
            return TransactionKind.INCOME;
        }
        throw new IllegalArgumentException(
            "transaction touches none of the user's accounts; cannot classify");
    }
}
