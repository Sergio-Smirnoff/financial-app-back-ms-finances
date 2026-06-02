package com.financialapp.finances.domain.service;

import com.financialapp.finances.domain.common.model.OwnedAccount;
import com.financialapp.finances.domain.exception.transaction.AccountCurrencyMismatchException;
import com.financialapp.finances.domain.model.transaction.Transaction;

import java.util.Currency;
import java.util.Set;

/**
 * Pure domain service: a transaction's currency must equal the currency of every account the user
 * owns on either side. External counterparties — whose currency ms-banks does not know — are not
 * checked. I/O-free: the application resolves the owned accounts via {@code AccountOwnershipGateway}
 * and passes them in.
 */
public final class TransactionCurrencyValidator {

    public void validate(Transaction transaction, Set<OwnedAccount> ownedAccounts) {
        Currency transactionCurrency = transaction.money().currency();
        for (OwnedAccount account : ownedAccounts) {
            if (transaction.involves(account.cbu()) && !transactionCurrency.equals(account.currency())) {
                throw new AccountCurrencyMismatchException(
                        account.cbu(), account.currency(), transactionCurrency);
            }
        }
    }
}
