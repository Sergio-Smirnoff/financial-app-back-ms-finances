package com.financialapp.finances.application.transaction.impl;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.common.model.Money;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.gateway.AccountOwnershipGateway;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.model.transaction.TransactionSummary;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.domain.service.TransactionClassifier;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetTransactionSummaryTotalsBranchesTest {

    private final Currency ars = Currency.getInstance("ARS");
    private final UserId user = new UserId(1L);
    private final Cbu mine = new Cbu("1111111111111111111111");
    private final Cbu mine2 = new Cbu("3333333333333333333333");
    private final Cbu other = new Cbu("2222222222222222222222");
    private final LocalDate day = LocalDate.parse("2026-05-10");

    private Transaction tx(Cbu from, Cbu to, String amount) {
        return Transaction.create(user, from, to, new Money(new BigDecimal(amount), ars),
                new CategoryId(1L), "x", day);
    }

    @Test void summary_accumulatesExpense_andIgnoresTransfer() {
        // Given an expense (debit from owned), an income (credit to owned) and a self-transfer (both owned)
        TransactionRepository repo = mock(TransactionRepository.class);
        when(repo.findByUser(user)).thenReturn(List.of(
                tx(mine, other, "30"),    // EXPENSE: only `mine` owned and it is debited
                tx(other, mine, "70"),    // INCOME: `mine` credited
                tx(mine, mine2, "999")));  // TRANSFER: both sides owned -> ignored
        AccountOwnershipGateway ownership = mock(AccountOwnershipGateway.class);
        when(ownership.ownedAccounts(user)).thenReturn(Set.of(mine, mine2));

        // When summarizing
        List<TransactionSummary> result = new GetTransactionSummaryUseCaseImpl(
                repo, ownership, new TransactionClassifier()).execute(user);

        // Then expense=30, income=70, balance=40, the transfer leaves totals untouched
        assertThat(result).hasSize(1);
        TransactionSummary s = result.get(0);
        assertThat(s.totalExpense()).isEqualByComparingTo("30");
        assertThat(s.totalIncome()).isEqualByComparingTo("70");
        assertThat(s.balance()).isEqualByComparingTo("40");
    }
}
