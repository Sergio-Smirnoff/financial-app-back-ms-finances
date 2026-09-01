package com.financialapp.finances.application.transaction.impl;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.commons.core.domain.model.Cbu;
import com.financialapp.finances.domain.common.model.DateRange;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetTransactionSummaryUseCaseImplTest {

    private final Currency ars = Currency.getInstance("ARS");
    private final UserId user = new UserId(1L);
    private final Cbu fromCbu = new Cbu("1111111111111111111111");
    private final Cbu toCbu = new Cbu("2222222222222222222222");

    /** A real transaction crediting toCbu; with toCbu owned, the classifier labels it INCOME. */
    private Transaction incomeTx(String amount, LocalDate date) {
        return Transaction.create(user, fromCbu, toCbu,
                new Money(new BigDecimal(amount), ars), new CategoryId(1L), "salary", date);
    }

    private GetTransactionSummaryUseCaseImpl useCase(TransactionRepository repo) {
        AccountOwnershipGateway ownership = mock(AccountOwnershipGateway.class);
        when(ownership.ownedAccounts(user)).thenReturn(Set.of(toCbu));   // owns the credited side
        return new GetTransactionSummaryUseCaseImpl(repo, ownership, new TransactionClassifier());
    }

    @Test
    void ranged_execute_reads_via_findByUserAndDateBetween() {
        DateRange range = new DateRange(LocalDate.parse("2026-05-01"), LocalDate.parse("2026-05-31"));
        TransactionRepository repo = mock(TransactionRepository.class);
        when(repo.findByUser(user)).thenReturn(List.of()); // all-time path empty
        when(repo.findByUserAndDateBetween(user, range.from(), range.to()))
                .thenReturn(List.of(incomeTx("100", LocalDate.parse("2026-05-10"))));

        List<TransactionSummary> summaries = useCase(repo).execute(user, range);

        verify(repo).findByUserAndDateBetween(user, range.from(), range.to());
        verify(repo, never()).findByUser(user);
        assertEquals(1, summaries.size());
        assertEquals(ars, summaries.get(0).currency());
        assertEquals(0, summaries.get(0).totalIncome().compareTo(new BigDecimal("100")));
    }

    @Test
    void all_time_execute_still_reads_via_findByUser() {
        TransactionRepository repo = mock(TransactionRepository.class);
        when(repo.findByUser(user)).thenReturn(List.of(incomeTx("50", LocalDate.parse("2026-03-03"))));

        List<TransactionSummary> summaries = useCase(repo).execute(user);

        verify(repo).findByUser(user);
        assertEquals(1, summaries.size());
        assertEquals(0, summaries.get(0).totalIncome().compareTo(new BigDecimal("50")));
    }
}
