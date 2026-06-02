package com.financialapp.finances.application.transaction.impl;

import com.financialapp.finances.domain.common.model.*;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.domain.usecase.transaction.command.UpdateTransactionCommand;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UpdateTransactionUseCaseImplTest {

    private static final Currency ARS = Currency.getInstance("ARS");
    private final TransactionRepository repo = mock(TransactionRepository.class);
    private final UpdateTransactionUseCaseImpl useCase = new UpdateTransactionUseCaseImpl(repo);

    private final Cbu mine = new Cbu("0001112223334445556667");
    private final Cbu other = new Cbu("9998887776665554443332");

    private Transaction existing() {
        return Transaction.reconstitute(new TransactionId(77L), new UserId(42L), mine, other,
                new Money(new BigDecimal("100.00"), ARS), new CategoryId(5L), "old",
                LocalDate.of(2026, 6, 1));
    }

    private UpdateTransactionCommand cmd(CategoryId categoryId, String description, LocalDate date) {
        return new UpdateTransactionCommand(new UserId(42L), new TransactionId(77L),
                categoryId, description, date);
    }

    @Test
    void appliesOnlyProvidedFieldsAndLeavesTheRestUnchanged() {
        when(repo.findByIdOwnedBy(new TransactionId(77L), new UserId(42L)))
                .thenReturn(Optional.of(existing()));
        when(repo.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        // only date provided
        Transaction saved = useCase.execute(cmd(null, null, LocalDate.of(2026, 4, 4)));

        assertThat(saved.date()).isEqualTo(LocalDate.of(2026, 4, 4));
        assertThat(saved.categoryId()).isEqualTo(new CategoryId(5L)); // unchanged
        assertThat(saved.description()).isEqualTo("old");             // unchanged
    }

    @Test
    void mergesMultipleProvidedFields() {
        when(repo.findByIdOwnedBy(new TransactionId(77L), new UserId(42L)))
                .thenReturn(Optional.of(existing()));
        when(repo.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction saved = useCase.execute(cmd(new CategoryId(9L), "hola", null));

        assertThat(saved.categoryId()).isEqualTo(new CategoryId(9L));
        assertThat(saved.description()).isEqualTo("hola");
        assertThat(saved.date()).isEqualTo(LocalDate.of(2026, 6, 1)); // unchanged
    }

    @Test
    void freezesMoneyAndEmitsNoBalanceEvents() {
        when(repo.findByIdOwnedBy(new TransactionId(77L), new UserId(42L)))
                .thenReturn(Optional.of(existing()));
        when(repo.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction saved = useCase.execute(cmd(new CategoryId(9L), null, null));

        assertThat(saved.money()).isEqualTo(new Money(new BigDecimal("100.00"), ARS));
        assertThat(saved.pullDomainEvents()).isEmpty();
    }

    @Test
    void missingTransactionThrows() {
        when(repo.findByIdOwnedBy(any(), any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(cmd(new CategoryId(9L), null, null)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repo, never()).save(any());
    }
}
