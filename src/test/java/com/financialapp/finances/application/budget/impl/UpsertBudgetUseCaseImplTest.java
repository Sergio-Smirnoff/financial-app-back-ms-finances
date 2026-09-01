package com.financialapp.finances.application.budget.impl;

import com.financialapp.finances.domain.common.model.BudgetId;
import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.Money;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.budget.Budget;
import com.financialapp.finances.domain.model.budget.BudgetPeriod;
import com.financialapp.finances.domain.model.category.CategoryNames;
import com.financialapp.finances.domain.repository.BudgetRepository;
import com.financialapp.finances.domain.repository.CategoryRepository;
import com.financialapp.finances.domain.usecase.budget.BudgetView;
import com.financialapp.finances.domain.usecase.budget.command.UpsertBudgetCommand;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UpsertBudgetUseCaseImplTest {

    private final BudgetRepository budgetRepository = mock(BudgetRepository.class);
    private final CategoryRepository categoryRepository = mock(CategoryRepository.class);
    private final UpsertBudgetUseCaseImpl useCase = new UpsertBudgetUseCaseImpl(budgetRepository, categoryRepository);

    @Test
    void upsertsBudgetAndResolvesCategoryName() {
        UserId userId = new UserId(42L);
        CategoryId categoryId = new CategoryId(10L);
        BudgetPeriod period = new BudgetPeriod(2026, 7);
        Money amount = new Money(new BigDecimal("50000.00"), Currency.getInstance("ARS"));
        UpsertBudgetCommand command = new UpsertBudgetCommand(userId, categoryId, period, amount, new BigDecimal("90.00"));

        Budget savedBudget = Budget.reconstitute(new BudgetId(1L), userId, categoryId, period, amount, new BigDecimal("90.00"), null);
        when(budgetRepository.upsert(any(Budget.class))).thenReturn(savedBudget);
        when(categoryRepository.findNamesById(categoryId)).thenReturn(Optional.of(new CategoryNames("Food", "Groceries")));

        BudgetView view = useCase.execute(command);

        assertThat(view.budget().id().value()).isEqualTo(1L);
        assertThat(view.categoryName()).isEqualTo("Groceries");
        verify(budgetRepository).upsert(any(Budget.class));
    }
}
