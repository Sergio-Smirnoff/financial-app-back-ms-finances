package com.financialapp.finances.infrastructure.persistence.repository;
import com.financialapp.commons.core.domain.model.Cbu;

import com.financialapp.finances.domain.common.model.*;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.infrastructure.persistence.entity.TransactionJpaEntity;
import com.financialapp.finances.infrastructure.persistence.jpa.TransactionJpaRepository;
import com.financialapp.finances.infrastructure.persistence.mapper.TransactionPersistenceMapper;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TransactionRepositoryImplQueryTest {

    private final TransactionJpaRepository jpa = mock(TransactionJpaRepository.class);
    private final NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
    private final SystemCategoryResolver systemCategoryResolver = mock(SystemCategoryResolver.class);
    private final TransactionRepositoryImpl repo =
            new TransactionRepositoryImpl(jpa, new TransactionPersistenceMapper(), jdbcTemplate, systemCategoryResolver);

    private TransactionJpaEntity entity(long id) {
        return TransactionJpaEntity.builder().id(id).userId(42L)
                .fromCbu("0001112223334445556667").toCbu("9998887776665554443332")
                .amount(new BigDecimal("100.00")).currency("ARS").categoryId(5L)
                .description("x").date(LocalDate.of(2026, 6, 1))
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void findByUserMapsAll() {
        when(jpa.findByUserIdOrderByDateDescIdDesc(42L)).thenReturn(List.of(entity(1), entity(2)));
        List<Transaction> result = repo.findByUser(new UserId(42L));
        assertThat(result).hasSize(2);
        assertThat(result.get(0).userId()).isEqualTo(new UserId(42L));
    }

    @Test
    void findByAccountDelegatesWithLimitAndRange() {
        when(jpa.findByAccount(eq("0001112223334445556667"), any(), any(), any()))
                .thenReturn(List.of(entity(1)));
        List<Transaction> result = repo.findByAccount(
                new Cbu("0001112223334445556667"), 5, null, null);
        assertThat(result).hasSize(1);
    }

    @Test
    void deleteRemovesById() {
        Transaction tx = repo.findByUser(new UserId(42L)).stream().findFirst()
                .orElse(Transaction.reconstitute(new TransactionId(9L), new UserId(42L),
                        new Cbu("0001112223334445556667"), new Cbu("9998887776665554443332"),
                        new Money(new BigDecimal("100.00"), Currency.getInstance("ARS")),
                        new CategoryId(5L), "x", LocalDate.of(2026, 6, 1)));
        repo.delete(tx);
        verify(jpa).deleteById(9L);
    }
}
