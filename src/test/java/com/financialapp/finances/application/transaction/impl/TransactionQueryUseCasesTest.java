package com.financialapp.finances.application.transaction.impl;

import com.financialapp.finances.domain.common.model.*;
import com.financialapp.finances.domain.gateway.AccountOwnershipGateway;
import com.financialapp.finances.domain.model.transaction.ClassifiedTransaction;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.model.transaction.TransactionKind;
import com.financialapp.finances.domain.model.transaction.TransactionSummary;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.domain.service.TransactionClassifier;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TransactionQueryUseCasesTest {

    private static final Currency ARS = Currency.getInstance("ARS");
    private final TransactionRepository repo = mock(TransactionRepository.class);
    private final AccountOwnershipGateway ownership = mock(AccountOwnershipGateway.class);
    private final TransactionClassifier classifier = new TransactionClassifier();

    private final ListUserTransactionsUseCaseImpl listUser =
            new ListUserTransactionsUseCaseImpl(repo, ownership, classifier);
    private final GetTransactionSummaryUseCaseImpl getSummary =
            new GetTransactionSummaryUseCaseImpl(repo, ownership, classifier);
    private final ListAccountTransactionsUseCaseImpl listAccount =
            new ListAccountTransactionsUseCaseImpl(repo);

    private final Cbu mine = new Cbu("0001112223334445556667");
    private final Cbu other = new Cbu("9998887776665554443332");

    private Transaction tx(long id, Cbu from, Cbu to, String amount) {
        return Transaction.reconstitute(new TransactionId(id), new UserId(42L), from, to,
                new Money(new BigDecimal(amount), ARS), new CategoryId(5L), "x", LocalDate.of(2026, 6, 1));
    }

    @Test
    void listUserClassifiesEachRow() {
        when(ownership.ownedAccounts(new UserId(42L))).thenReturn(Set.of(mine));
        when(repo.findByUser(new UserId(42L))).thenReturn(List.of(tx(1, mine, other, "100.00")));

        List<ClassifiedTransaction> rows = listUser.execute(new UserId(42L));

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).kind()).isEqualTo(TransactionKind.EXPENSE);
    }

    @Test
    void summaryNetsIncomeMinusExpenseIgnoringTransfers() {
        when(ownership.ownedAccounts(new UserId(42L))).thenReturn(Set.of(mine));
        when(repo.findByUser(new UserId(42L))).thenReturn(List.of(
                tx(1, mine, other, "100.00"),   // expense (owns source)
                tx(2, other, mine, "30.00")));  // income  (owns destination)

        TransactionSummary summary = getSummary.execute(new UserId(42L));

        assertThat(summary.totalExpense()).isEqualByComparingTo("100.00");
        assertThat(summary.totalIncome()).isEqualByComparingTo("30.00");
        assertThat(summary.balance()).isEqualByComparingTo("-70.00");
    }

    @Test
    void accountListReturnsAggregates() {
        when(repo.findByAccount(mine, 5, null, null)).thenReturn(List.of(tx(1, mine, other, "100.00")));
        assertThat(listAccount.execute(mine, 5, null, null)).hasSize(1);
    }
}
