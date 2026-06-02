package com.financialapp.finances.domain.service;

import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.Money;
import com.financialapp.finances.domain.common.model.OwnedAccount;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.exception.transaction.AccountCurrencyMismatchException;
import com.financialapp.finances.domain.model.transaction.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionCurrencyValidatorTest {

    private static final Currency ARS = Currency.getInstance("ARS");
    private static final Currency USD = Currency.getInstance("USD");
    private final Cbu from = new Cbu("0001112223334445556667");
    private final Cbu to = new Cbu("9998887776665554443332");
    private final Cbu unrelated = new Cbu("1111111111111111111111");
    private final TransactionCurrencyValidator validator = new TransactionCurrencyValidator();

    private Transaction arsTx() {
        return Transaction.create(new UserId(1L), from, to,
                new Money(new BigDecimal("100.00"), ARS), new CategoryId(5L), "x", LocalDate.of(2026, 6, 1));
    }

    @Test void passes_whenOwnedAccountMatchesCurrency() {
        // Given an owned account on the tx in the same currency
        assertThatCode(() -> validator.validate(arsTx(), Set.of(new OwnedAccount(from, ARS))))
                .doesNotThrowAnyException();
    }

    @Test void passes_whenMismatchedAccountIsNotInvolved() {
        // Given a mismatched-currency account that the tx does not involve (short-circuits the &&)
        assertThatCode(() -> validator.validate(arsTx(), Set.of(new OwnedAccount(unrelated, USD))))
                .doesNotThrowAnyException();
    }

    @Test void rejects_whenInvolvedOwnedAccountHasOtherCurrency() {
        // Given an owned, involved account in a different currency
        assertThatThrownBy(() -> validator.validate(arsTx(), Set.of(new OwnedAccount(from, USD))))
                .isInstanceOf(AccountCurrencyMismatchException.class);
    }
}
