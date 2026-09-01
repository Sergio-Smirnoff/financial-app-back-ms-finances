package com.financialapp.finances.application.transaction.impl;
import com.financialapp.commons.core.domain.model.Cbu;

import com.financialapp.finances.domain.common.model.*;
import com.financialapp.finances.domain.exception.transaction.TransactionNotFoundException;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.repository.TransactionRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetTransactionDetailUseCaseImplTest {

    private final TransactionRepository repository = mock(TransactionRepository.class);
    private final GetTransactionDetailUseCaseImpl useCase = new GetTransactionDetailUseCaseImpl(repository);

    @Test
    void returnsTransactionWhenOwnedByUser() {
        TransactionId id = new TransactionId(10L);
        UserId userId = new UserId(42L);
        Transaction tx = Transaction.create(userId, new Cbu("0001112223334445556667"),
                new Cbu("9998887776665554443332"), new Money(new BigDecimal("100.00"), Currency.getInstance("ARS")),
                new CategoryId(1L), "Test", LocalDate.now());

        when(repository.findByIdOwnedBy(id, userId)).thenReturn(Optional.of(tx));

        Transaction result = useCase.execute(id, userId);

        assertThat(result).isEqualTo(tx);
    }

    @Test
    void throwsNotFoundWhenTransactionMissingOrUnowned() {
        TransactionId id = new TransactionId(10L);
        UserId userId = new UserId(42L);

        when(repository.findByIdOwnedBy(id, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(id, userId))
                .isInstanceOf(TransactionNotFoundException.class);
    }
}
