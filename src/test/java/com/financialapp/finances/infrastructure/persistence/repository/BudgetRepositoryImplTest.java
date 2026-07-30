package com.financialapp.finances.infrastructure.persistence.repository;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.Money;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.budget.Budget;
import com.financialapp.finances.domain.model.budget.BudgetPeriod;
import com.financialapp.finances.infrastructure.persistence.entity.BudgetJpaEntity;
import com.financialapp.finances.infrastructure.persistence.jpa.BudgetJpaRepository;
import com.financialapp.finances.infrastructure.persistence.mapper.BudgetPersistenceMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class BudgetRepositoryImplTest {

    private final BudgetJpaRepository jpa = mock(BudgetJpaRepository.class);
    private final BudgetPersistenceMapper mapper = new BudgetPersistenceMapper();
    private final BudgetRepositoryImpl repo = new BudgetRepositoryImpl(jpa, mapper);

    private static final UserId USER_42 = new UserId(42L);
    private static final CategoryId CATEGORY_10 = new CategoryId(10L);
    private static final BudgetPeriod PERIOD_JULY = new BudgetPeriod(2026, 7);
    private static final Currency ARS = Currency.getInstance("ARS");

    @Test
    void upsertDelegatesToJpaNativeQueryAndFindsSaved() {
        Budget budget = Budget.create(USER_42, CATEGORY_10, PERIOD_JULY, new Money(new BigDecimal("50000.00"), ARS), new BigDecimal("90.00"));
        BudgetJpaEntity savedEntity = mapper.toEntity(budget);
        savedEntity.setId(1L);

        when(jpa.findByUserIdAndCategoryIdAndYearAndMonth(42L, 10L, 2026, 7))
                .thenReturn(Optional.of(savedEntity));

        Budget saved = repo.upsert(budget);

        verify(jpa).upsertBudget(
                eq(42L), eq(10L), eq(2026), eq(7),
                eq(new BigDecimal("50000.00")), eq("ARS"),
                eq(new BigDecimal("90.00")),
                isNull(), isNull()
        );
        assertThat(saved.id().value()).isEqualTo(1L);
        assertThat(saved.amount()).isEqualTo(new Money(new BigDecimal("50000.00"), ARS));
        assertThat(saved.alertThresholdPct()).isEqualTo(new BigDecimal("90.00"));
    }

    @Test
    void findByUserAndPeriodReturnsMatchingBudgets() {
        Budget b1 = Budget.create(USER_42, CATEGORY_10, PERIOD_JULY, new Money(new BigDecimal("50000.00"), ARS), null);
        BudgetJpaEntity e1 = mapper.toEntity(b1);
        e1.setId(1L);

        when(jpa.findByUserIdAndYearAndMonth(42L, 2026, 7)).thenReturn(List.of(e1));

        List<Budget> result = repo.findByUserAndPeriod(USER_42, PERIOD_JULY);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id().value()).isEqualTo(1L);
    }

    @Test
    void findByUserCategoryAndPeriodReturnsBudget() {
        Budget b1 = Budget.create(USER_42, CATEGORY_10, PERIOD_JULY, new Money(new BigDecimal("50000.00"), ARS), null);
        BudgetJpaEntity e1 = mapper.toEntity(b1);
        e1.setId(1L);

        when(jpa.findByUserIdAndCategoryIdAndYearAndMonth(42L, 10L, 2026, 7)).thenReturn(Optional.of(e1));

        Optional<Budget> result = repo.findByUserCategoryAndPeriod(USER_42, CATEGORY_10, PERIOD_JULY);

        assertThat(result).isPresent();
        assertThat(result.get().id().value()).isEqualTo(1L);
    }
}
