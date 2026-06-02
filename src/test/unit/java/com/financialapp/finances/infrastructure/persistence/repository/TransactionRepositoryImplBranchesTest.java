package com.financialapp.finances.infrastructure.persistence.repository;

import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.infrastructure.persistence.entity.TransactionJpaEntity;
import com.financialapp.finances.infrastructure.persistence.jpa.TransactionJpaRepository;
import com.financialapp.finances.infrastructure.persistence.mapper.TransactionPersistenceMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Limit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TransactionRepositoryImplBranchesTest {

    private final TransactionJpaRepository jpa = mock(TransactionJpaRepository.class);
    private final TransactionRepositoryImpl repo =
            new TransactionRepositoryImpl(jpa, new TransactionPersistenceMapper());

    private TransactionJpaEntity entity(long id) {
        return TransactionJpaEntity.builder().id(id).userId(42L)
                .fromCbu("0001112223334445556667").toCbu("9998887776665554443332")
                .amount(new BigDecimal("100.00")).currency("ARS").categoryId(5L)
                .description("x").date(LocalDate.of(2026, 6, 1))
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
    }

    @Test void findByUserAndDateBetween_delegatesToOrderedQuery() {
        LocalDate from = LocalDate.of(2026, 5, 1);
        LocalDate to = LocalDate.of(2026, 5, 31);
        when(jpa.findByUserIdAndDateBetweenOrderByDateDescIdDesc(42L, from, to)).thenReturn(List.of(entity(1)));
        List<Transaction> result = repo.findByUserAndDateBetween(new UserId(42L), from, to);
        assertThat(result).hasSize(1);
    }

    @Test void findByAccount_usesUnlimited_whenLimitNull() {
        // Given a null limit (the Limit.unlimited() branch)
        when(jpa.findByAccount(eq("0001112223334445556667"), any(), any(), eq(Limit.unlimited())))
                .thenReturn(List.of(entity(1)));
        List<Transaction> result = repo.findByAccount(new Cbu("0001112223334445556667"), null, null, null);
        assertThat(result).hasSize(1);
        verify(jpa).findByAccount(eq("0001112223334445556667"), any(), any(), eq(Limit.unlimited()));
    }

    @Test void existsDuplicate_delegatesToJpa() {
        when(jpa.existsDuplicate(eq(42L), anyString(), anyString(), any(), eq("ARS"), any(), eq("x")))
                .thenReturn(true);
        boolean dup = repo.existsDuplicate(new UserId(42L),
                new Cbu("0001112223334445556667"), new Cbu("9998887776665554443332"),
                new BigDecimal("100.00"), "ARS", LocalDate.of(2026, 6, 1), "x");
        assertThat(dup).isTrue();
    }
}
