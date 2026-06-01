package com.financialapp.finances.application.transaction.impl;

import com.financialapp.finances.domain.common.model.*;
import com.financialapp.finances.domain.event.DomainEvent;
import com.financialapp.finances.domain.event.TransactionCreated;
import com.financialapp.finances.domain.exception.transaction.UnownedTransactionException;
import com.financialapp.finances.domain.gateway.AccountOwnershipGateway;
import com.financialapp.finances.domain.gateway.DomainEventPublisher;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.domain.service.TransactionPosting;
import com.financialapp.finances.domain.usecase.transaction.command.RecordTransactionCommand;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.Currency;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RecordTransactionUseCaseImplTest {

    private static final Currency ARS = Currency.getInstance("ARS");
    private final TransactionRepository repo = mock(TransactionRepository.class);
    private final AccountOwnershipGateway ownership = mock(AccountOwnershipGateway.class);
    private final DomainEventPublisher publisher = mock(DomainEventPublisher.class);
    private final RecordTransactionUseCaseImpl useCase =
            new RecordTransactionUseCaseImpl(repo, ownership, new TransactionPosting(), publisher);

    private final Cbu mine = new Cbu("0001112223334445556667");
    private final Cbu other = new Cbu("9998887776665554443332");

    private RecordTransactionCommand cmd(Cbu from, Cbu to) {
        return new RecordTransactionCommand(new UserId(42L), from, to,
                new Money(new BigDecimal("100.00"), ARS), new CategoryId(5L), "x",
                LocalDate.of(2026, 6, 1));
    }

    private void echoSaveWithId(long id) {
        when(repo.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            return Transaction.reconstitute(new TransactionId(id), t.userId(), t.fromCbu(), t.toCbu(),
                    t.money(), t.categoryId(), t.description(), t.date());
        });
    }

    @Test
    void expenseEmitsOneBalanceEvent() {
        when(ownership.ownedAccounts(new UserId(42L))).thenReturn(Set.of(mine));
        echoSaveWithId(77L);

        useCase.execute(cmd(mine, other));   // from owned, to external => expense

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DomainEvent>> cap = ArgumentCaptor.forClass(List.class);
        verify(publisher).publishAll(cap.capture());
        assertThat(cap.getValue()).hasSize(1);
        TransactionCreated e = (TransactionCreated) cap.getValue().get(0);
        assertThat(e.accountCbu()).isEqualTo(mine);
        assertThat(e.signedAmount()).isEqualByComparingTo("-100.00");
    }

    @Test
    void transferBetweenOwnedAccountsEmitsTwoBalanceEvents() {
        when(ownership.ownedAccounts(new UserId(42L))).thenReturn(Set.of(mine, other));
        echoSaveWithId(77L);

        useCase.execute(cmd(mine, other));   // both owned => transfer

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DomainEvent>> cap = ArgumentCaptor.forClass(List.class);
        verify(publisher).publishAll(cap.capture());
        assertThat(cap.getValue()).hasSize(2);
    }

    @Test
    void noneOwnedThrowsAndNeitherSavesNorPublishes() {
        when(ownership.ownedAccounts(new UserId(42L))).thenReturn(Set.of());

        assertThatThrownBy(() -> useCase.execute(cmd(mine, other)))
                .isInstanceOf(UnownedTransactionException.class);

        verify(repo, never()).save(any());
        verify(publisher, never()).publishAll(any());
    }
}
