package com.financialapp.finances.infrastructure.persistence.repository;
import com.financialapp.commons.core.domain.model.Cbu;

import com.financialapp.finances.domain.common.model.*;
import com.financialapp.finances.domain.model.transaction.CursorPage;
import com.financialapp.finances.domain.model.transaction.DescriptionQuery;
import com.financialapp.finances.domain.model.transaction.PaymentMethod;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.model.transaction.TransactionFilter;
import com.financialapp.finances.infrastructure.persistence.entity.TransactionJpaEntity;
import com.financialapp.finances.infrastructure.persistence.jpa.TransactionJpaRepository;
import com.financialapp.finances.infrastructure.persistence.mapper.TransactionPersistenceMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.data.domain.Limit;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TransactionRepositoryImplQueryTest {

    private final TransactionJpaRepository jpa = mock(TransactionJpaRepository.class);
    private final NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
    private final SystemCategoryResolver systemCategoryResolver = mock(SystemCategoryResolver.class);
    private final com.financialapp.finances.domain.gateway.AccountOwnershipGateway ownershipGateway = mock(com.financialapp.finances.domain.gateway.AccountOwnershipGateway.class);
    private final TransactionRepositoryImpl repo =
            new TransactionRepositoryImpl(jpa, new TransactionPersistenceMapper(), jdbcTemplate, systemCategoryResolver, ownershipGateway);

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

    @Test
    void findFilteredBindsListMethodAndDescriptionPredicates() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(0L);
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), ArgumentMatchers.<RowMapper<Transaction>>any()))
                .thenReturn(List.of());

        TransactionFilter filter = new TransactionFilter(
                new UserId(42L),
                Set.of(new Cbu("0001112223334445556667")),
                List.of(new Cbu("0001112223334445556667")),
                List.of(new CategoryId(5L), new CategoryId(9L)),
                null, null, false, null, null,
                PaymentMethod.CREDIT_CARD,
                new DescriptionQuery("super"));

        repo.findFiltered(filter, new CursorPage(null, 20));

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> params = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).query(sql.capture(), params.capture(), ArgumentMatchers.<RowMapper<Transaction>>any());

        assertThat(sql.getValue()).contains("t.category_id IN (:categoryIds)");
        assertThat(sql.getValue()).contains("t.from_cbu IN (:accountCbus) OR t.to_cbu IN (:accountCbus)");
        assertThat(sql.getValue()).contains("COALESCE(t.payment_method, 'OTHER') = :paymentMethod");
        assertThat(sql.getValue()).contains("LOWER(t.description) LIKE LOWER(:descriptionQuery)");
        assertThat(params.getValue().getValue("categoryIds")).isEqualTo(List.of(5L, 9L));
        assertThat(params.getValue().getValue("accountCbus")).isEqualTo(List.of("0001112223334445556667"));
        assertThat(params.getValue().getValue("paymentMethod")).isEqualTo("CREDIT_CARD");
        assertThat(params.getValue().getValue("descriptionQuery")).isEqualTo("%super%");
    }

    @Test
    void findFilteredAppendsAnOffsetWhenNoCursorIsGiven() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(0L);
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), ArgumentMatchers.<RowMapper<Transaction>>any()))
                .thenReturn(List.of());

        TransactionFilter filter = new TransactionFilter(
                new UserId(42L), Set.of(), List.of(), List.of(), null, null, false, null, null, null, null);

        repo.findFiltered(filter, CursorPage.ofPage(null, 20, 2));

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> params = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).query(sql.capture(), params.capture(), ArgumentMatchers.<RowMapper<Transaction>>any());

        assertThat(sql.getValue()).contains("LIMIT :pageSize OFFSET :rowOffset");
        assertThat(params.getValue().getValue("rowOffset")).isEqualTo(40);
    }

    @Test
    void findFilteredBindsAnEscapedContainsPatternWithAnEscapeClause() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(0L);
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), ArgumentMatchers.<RowMapper<Transaction>>any()))
                .thenReturn(List.of());

        TransactionFilter filter = new TransactionFilter(
                new UserId(42L), Set.of(), List.of(), List.of(),
                null, null, false, null, null, null, new DescriptionQuery("50%"));

        repo.findFiltered(filter, new CursorPage(null, 20));

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> params = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).query(sql.capture(), params.capture(), ArgumentMatchers.<RowMapper<Transaction>>any());

        assertThat(sql.getValue()).contains("LOWER(t.description) LIKE LOWER(:descriptionQuery) ESCAPE '\\'");
        assertThat(params.getValue().getValue("descriptionQuery")).isEqualTo("%50\\%%");
    }

    @Test
    void searchByDescriptionPassesAnEscapedContainsPattern() {
        when(jpa.searchByDescription(anyLong(), anyString(), any(Limit.class))).thenReturn(List.of());

        repo.searchByDescription(new UserId(42L), "50%", 10);

        verify(jpa).searchByDescription(eq(42L), eq("%50\\%%"), eq(Limit.of(10)));
    }

    @Test
    void uncategorisedIsUnionedWithTheSelectedCategories() {
        when(systemCategoryResolver.findUnassignedCategoryId()).thenReturn(Optional.of(99L));
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(0L);
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), ArgumentMatchers.<RowMapper<Transaction>>any()))
                .thenReturn(List.of());

        TransactionFilter filter = new TransactionFilter(
                new UserId(42L), Set.of(), List.of(),
                List.of(new CategoryId(5L)),
                null, null, true, null, null, null, null);

        repo.findFiltered(filter, new CursorPage(null, 20));

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> params = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).query(sql.capture(), params.capture(), ArgumentMatchers.<RowMapper<Transaction>>any());

        assertThat(sql.getValue()).containsOnlyOnce("t.category_id IN (:categoryIds)");
        assertThat(sql.getValue()).doesNotContain(":unassignedId");
        assertThat(params.getValue().getValue("categoryIds")).isEqualTo(List.of(5L, 99L));
    }

    @Test
    void uncategorisedAloneStillFiltersToTheUnassignedCategory() {
        when(systemCategoryResolver.findUnassignedCategoryId()).thenReturn(Optional.of(99L));
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(0L);
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), ArgumentMatchers.<RowMapper<Transaction>>any()))
                .thenReturn(List.of());

        TransactionFilter filter = new TransactionFilter(
                new UserId(42L), Set.of(), List.of(), List.of(),
                null, null, true, null, null, null, null);

        repo.findFiltered(filter, new CursorPage(null, 20));

        ArgumentCaptor<MapSqlParameterSource> params = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).query(anyString(), params.capture(), ArgumentMatchers.<RowMapper<Transaction>>any());
        assertThat(params.getValue().getValue("categoryIds")).isEqualTo(List.of(99L));
    }
}
