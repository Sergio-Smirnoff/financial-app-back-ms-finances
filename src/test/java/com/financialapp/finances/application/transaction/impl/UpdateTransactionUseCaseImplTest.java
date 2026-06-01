package com.financialapp.finances.application.transaction.impl;

import com.financialapp.finances.domain.common.model.*;
import com.financialapp.finances.domain.event.DomainEvent;
import com.financialapp.finances.domain.event.TransactionCreated;
import com.financialapp.finances.domain.event.TransactionReversed;
import com.financialapp.finances.domain.gateway.AccountOwnershipGateway;
import com.financialapp.finances.domain.gateway.DomainEventPublisher;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.domain.service.TransactionPosting;
import com.financialapp.finances.domain.usecase.transaction.command.UpdateTransactionCommand;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Currency;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UpdateTransactionUseCaseImplTest {

    private static final Currency ARS = Currency.getInstance("ARS");
    private final TransactionRepository repo = mock(TransactionRepository.class);
    private final AccountOwnershipGateway ownership = mock(AccountOwnershipGateway.class);
    private final DomainEventPublisher publisher = mock(DomainEventPublisher.class);
    private final UpdateTransactionUseCaseImpl useCase =
            new UpdateTransactionUseCaseImpl(repo, ownership, new TransactionPosting(), publisher);

    private final Cbu mine = new Cbu("0001112223334445556667");
    private final Cbu other = new Cbu("9998887776665554443332");

    private Transaction existing(String amount) {
        return Transaction.reconstitute(new TransactionId(77L), new UserId(42L), mine, other,
                new Money(new BigDecimal(amount), ARS), new CategoryId(5L), "old", LocalDate.of(2026, 6, 1));
    }

    private UpdateTransactionCommand cmd(String newAmount) {
        return new UpdateTransactionCommand(new UserId(42L), new TransactionId(77L),
                new Money(new BigDecimal(newAmount), ARS), new CategoryId(5L), "new", LocalDate.of(2026, 6, 1));
    }

    @Test
    void moneyChangeReversesOldAndAppliesNew() {
        when(repo.findByIdOwnedBy(new TransactionId(77L), new UserId(42L)))
                .thenReturn(Optional.of(existing("100.00")));
        when(ownership.ownedAccounts(new UserId(42L))).thenReturn(Set.of(mine));
        when(repo.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(cmd("150.00"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DomainEvent>> cap = ArgumentCaptor.forClass(List.class);
        verify(publisher).publishAll(cap.capture());
        List<DomainEvent> events = cap.getValue();
        assertThat(events).hasSize(2);
        assertThat(events.get(0)).isInstanceOf(TransactionReversed.class);
        assertThat(((TransactionReversed) events.get(0)).signedAmount()).isEqualByComparingTo("100.00");
        assertThat(events.get(1)).isInstanceOf(TransactionCreated.class);
        assertThat(((TransactionCreated) events.get(1)).signedAmount()).isEqualByComparingTo("-150.00");
    }

    @Test
    void missingTransactionThrows() {
        when(repo.findByIdOwnedBy(any(), any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(cmd("150.00")))
                .isInstanceOf(IllegalArgumentException.class);
        verify(publisher, never()).publishAll(any());
    }
}
