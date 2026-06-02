package com.financialapp.finances.application.transaction.impl;

import com.financialapp.finances.domain.common.model.*;
import com.financialapp.finances.domain.gateway.AccountOwnershipGateway;
import com.financialapp.finances.domain.model.transaction.ClassifiedTransaction;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.model.transaction.TransactionKind;
import com.financialapp.finances.domain.model.transaction.TransactionSummary;
import com.financialapp.finances.domain.model.category.CategoryNames;
import com.financialapp.finances.domain.repository.CategoryRepository;
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
    private final CategoryRepository categoryRepo = mock(CategoryRepository.class);
    private final AccountOwnershipGateway ownership = mock(AccountOwnershipGateway.class);
    private final TransactionClassifier classifier = new TransactionClassifier();

    private final ListUserTransactionsUseCaseImpl listUser =
            new ListUserTransactionsUseCaseImpl(repo, ownership, classifier, categoryRepo);
    private final GetTransactionSummaryUseCaseImpl getSummary =
            new GetTransactionSummaryUseCaseImpl(repo, ownership, classifier);
    private final ListAccountTransactionsUseCaseImpl listAccount =
            new ListAccountTransactionsUseCaseImpl(repo, categoryRepo);

    private final Cbu mine = new Cbu("0001112223334445556667");
    private final Cbu other = new Cbu("9998887776665554443332");

    private Transaction tx(long id, Cbu from, Cbu to, String amount) {
        return txIn(id, from, to, amount, ARS);
    }

    private Transaction txIn(long id, Cbu from, Cbu to, String amount, Currency currency) {
        return Transaction.reconstitute(new TransactionId(id), new UserId(42L), from, to,
                new Money(new BigDecimal(amount), currency), new CategoryId(5L), "x", LocalDate.of(2026, 6, 1));
    }

    @Test
    void listUserClassifiesEachRow() {
        when(ownership.ownedAccounts(new UserId(42L))).thenReturn(Set.of(mine));
        when(repo.findByUser(new UserId(42L))).thenReturn(List.of(tx(1, mine, other, "100.00")));

        var rows = listUser.execute(new UserId(42L));

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).classified().kind()).isEqualTo(TransactionKind.EXPENSE);
    }

    @Test
    void summaryNetsIncomeMinusExpenseIgnoringTransfers() {
        when(ownership.ownedAccounts(new UserId(42L))).thenReturn(Set.of(mine));
        when(repo.findByUser(new UserId(42L))).thenReturn(List.of(
                tx(1, mine, other, "100.00"),   // expense (owns source)
                tx(2, other, mine, "30.00")));  // income  (owns destination)

        List<TransactionSummary> summaries = getSummary.execute(new UserId(42L));

        assertThat(summaries).hasSize(1);
        TransactionSummary summary = summaries.get(0);
        assertThat(summary.currency()).isEqualTo(ARS);
        assertThat(summary.totalExpense()).isEqualByComparingTo("100.00");
        assertThat(summary.totalIncome()).isEqualByComparingTo("30.00");
        assertThat(summary.balance()).isEqualByComparingTo("-70.00");
    }

    @Test
    void summaryKeepsEachCurrencySeparateNeverSummingAcrossThem() {
        Currency usd = Currency.getInstance("USD");
        when(ownership.ownedAccounts(new UserId(42L))).thenReturn(Set.of(mine));
        when(repo.findByUser(new UserId(42L))).thenReturn(List.of(
                txIn(1, other, mine, "1000.00", ARS),   // income ARS
                txIn(2, other, mine, "100.00", usd)));  // income USD

        List<TransactionSummary> summaries = getSummary.execute(new UserId(42L));

        assertThat(summaries).hasSize(2);
        TransactionSummary ars = summaries.stream()
                .filter(s -> s.currency().equals(ARS)).findFirst().orElseThrow();
        TransactionSummary usdSummary = summaries.stream()
                .filter(s -> s.currency().equals(usd)).findFirst().orElseThrow();
        assertThat(ars.totalIncome()).isEqualByComparingTo("1000.00");
        assertThat(ars.balance()).isEqualByComparingTo("1000.00");
        assertThat(usdSummary.totalIncome()).isEqualByComparingTo("100.00");
        assertThat(usdSummary.balance()).isEqualByComparingTo("100.00");
    }

    @Test
    void accountListPairsEachTransactionWithResolvedNames() {
        when(repo.findByAccount(mine, 5, null, null)).thenReturn(List.of(tx(1, mine, other, "100.00")));
        when(categoryRepo.findNamesById(new CategoryId(5L)))
                .thenReturn(java.util.Optional.of(new CategoryNames("Housing", "Rent")));

        var rows = listAccount.execute(mine, 5, null, null);

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).transaction().id().value()).isEqualTo(1L);
        assertThat(rows.get(0).names()).isEqualTo(new CategoryNames("Housing", "Rent"));
    }

    @Test
    void accountListFallsBackToNullNamesWhenCategoryUnknown() {
        when(repo.findByAccount(mine, 5, null, null)).thenReturn(List.of(tx(1, mine, other, "100.00")));
        when(categoryRepo.findNamesById(new CategoryId(5L))).thenReturn(java.util.Optional.empty());

        assertThat(listAccount.execute(mine, 5, null, null).get(0).names())
                .isEqualTo(new CategoryNames(null, null));
    }
}
