package com.financialapp.finances.domain.exception.transaction;

import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.exception.DomainErrorCode;
import com.financialapp.finances.domain.exception.DomainException;

import java.util.Currency;
import java.util.Map;

/**
 * A transaction's currency must match the currency of every account the user owns on either side.
 * One owned side is held in a different currency than the transaction.
 */
public class AccountCurrencyMismatchException extends DomainException {
    public AccountCurrencyMismatchException(Cbu cbu, Currency accountCurrency, Currency transactionCurrency) {
        super(DomainErrorCode.ACCOUNT_CURRENCY_MISMATCH,
              "Account " + cbu.cbuNumber() + " is in " + accountCurrency.getCurrencyCode()
                  + " but the transaction is in " + transactionCurrency.getCurrencyCode(),
              Map.of("cbu", cbu.cbuNumber(),
                     "accountCurrency", accountCurrency.getCurrencyCode(),
                     "transactionCurrency", transactionCurrency.getCurrencyCode()));
    }
}
