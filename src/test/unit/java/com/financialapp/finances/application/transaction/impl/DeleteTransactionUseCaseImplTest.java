package com.financialapp.finances.application.transaction.impl;

import com.financialapp.finances.domain.common.model.*;
import com.financialapp.finances.domain.event.DomainEvent;
import com.financialapp.finances.domain.event.TransactionReversed;
import com.financialapp.finances.domain.gateway.AccountOwnershipGateway;
import com.financialapp.finances.domain.gateway.DomainEventPublisher;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.domain.service.TransactionPosting;
import com.financialapp.finances.domain.usecase.transaction.command.DeleteTransactionCommand;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Currency;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DeleteTransactionUseCaseImplTest {

    private static final Currency ARS = Currency.getInstance("ARS");
    private final TransactionRepository repo = mock(TransactionRepository.class);
    private final AccountOwnershipGateway ownership = mock(AccountOwnershipGateway.class);
    private final DomainEventPublisher publisher = mock(DomainEventPublisher.class);
    private final DeleteTransactionUseCaseImpl useCase =
            new DeleteTransactionUseCaseImpl(repo, ownership, new TransactionPosting(), publisher);

    private final Cbu mine = new Cbu("0001112223334445556667");
    private final Cbu other = new Cbu("9998887776665554443332");

    private Transaction existing() {
        return Transaction.reconstitute(new TransactionId(77L), new UserId(42L), mine, other,
                new Money(new BigDecimal("100.00"), ARS), new CategoryId(5L), "x", LocalDate.of(2026, 6, 1));
    }

    @Test
    void publishesReversalThenDeletes() {
        Transaction tx = existing();
        when(repo.findByIdOwnedBy(new TransactionId(77L), new UserId(42L))).thenReturn(Optional.of(tx));
        when(ownership.ownedAccounts(new UserId(42L))).thenReturn(Set.of(mine));

        useCase.execute(new DeleteTransactionCommand(new UserId(42L), new TransactionId(77L)));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DomainEvent>> cap = ArgumentCaptor.forClass(List.class);
        InOrder inOrder = inOrder(publisher, repo);
        inOrder.verify(publisher).publishAll(cap.capture());
        inOrder.verify(repo).delete(tx);
        assertThat(cap.getValue()).hasSize(1);
        assertThat(cap.getValue().get(0)).isInstanceOf(TransactionReversed.class);
        assertThat(((TransactionReversed) cap.getValue().get(0)).signedAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    void missingTransactionThrowsAndDeletesNothing() {
        when(repo.findByIdOwnedBy(any(), any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(new DeleteTransactionCommand(new UserId(42L), new TransactionId(1L))))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repo, never()).delete(any());
    }
}
