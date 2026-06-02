package com.financialapp.finances.infrastructure.persistence.repository;

import com.financialapp.finances.domain.common.model.*;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.infrastructure.persistence.entity.TransactionJpaEntity;
import com.financialapp.finances.infrastructure.persistence.jpa.TransactionJpaRepository;
import com.financialapp.finances.infrastructure.persistence.mapper.TransactionPersistenceMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionRepositoryImplTest {

    private final TransactionJpaRepository jpa = mock(TransactionJpaRepository.class);
    private final TransactionRepositoryImpl repo =
            new TransactionRepositoryImpl(jpa, new TransactionPersistenceMapper());
    private static final Currency ARS = Currency.getInstance("ARS");

    private Transaction newTx() {
        return Transaction.create(new UserId(42L),
                new Cbu("0001112223334445556667"), new Cbu("9998887776665554443332"),
                new Money(new BigDecimal("100.00"), ARS), new CategoryId(5L), "x",
                LocalDate.of(2026, 6, 1));
    }

    @Test
    void saveReturnsReconstitutedAggregateWithGeneratedId() {
        when(jpa.save(any(TransactionJpaEntity.class))).thenAnswer(inv -> {
            TransactionJpaEntity e = inv.getArgument(0);
            e.setId(77L);
            return e;
        });

        Transaction saved = repo.save(newTx());

        assertThat(saved.id()).isEqualTo(new TransactionId(77L));
        verify(jpa).save(any(TransactionJpaEntity.class));
    }

    @Test
    void findByIdOwnedByScopesToUser() {
        TransactionJpaEntity e = new TransactionPersistenceMapper().toEntity(newTx());
        e.setId(77L);
        when(jpa.findByIdAndUserId(77L, 42L)).thenReturn(Optional.of(e));

        Optional<Transaction> found = repo.findByIdOwnedBy(new TransactionId(77L), new UserId(42L));

        assertThat(found).isPresent();
        assertThat(found.get().id()).isEqualTo(new TransactionId(77L));
    }
}
