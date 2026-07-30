package com.financialapp.finances.domain.service;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.Money;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.exception.budget.InvalidBudgetException;
import com.financialapp.finances.domain.model.budget.Budget;
import com.financialapp.finances.domain.model.budget.BudgetPeriod;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BudgetPaceTest {

    private static final UserId USER_ID = new UserId(1L);
    private static final CategoryId CATEGORY_ID = new CategoryId(10L);
    private static final BudgetPeriod PERIOD_JULY_2026 = new BudgetPeriod(2026, 7);
    private static final Currency ARS = Currency.getInstance("ARS");
    private static final Currency USD = Currency.getInstance("USD");

    private final BudgetPace budgetPace = new BudgetPace();

    @Test
    void evaluatesMidMonthPace() {
        Budget budget = Budget.create(USER_ID, CATEGORY_ID, PERIOD_JULY_2026, new Money(new BigDecimal("100000.00"), ARS), null);
        Money spend = new Money(new BigDecimal("30000.00"), ARS);
        LocalDate today = LocalDate.of(2026, 7, 15);

        BudgetPaceResult result = budgetPace.evaluate(budget, spend, today);

        assertThat(result.spent()).isEqualTo(spend);
        assertThat(result.remaining()).isEqualTo(new Money(new BigDecimal("70000.00"), ARS));
        assertThat(result.pctUsed()).isEqualTo(new BigDecimal("30.00"));
        assertThat(result.expectedPctByToday()).isEqualTo(new BigDecimal("48.39")); // 15/31 * 100
        assertThat(result.overBudget()).isFalse();
    }

    @Test
    void evaluatesStartAndEndOfMonthPace() {
        Budget budget = Budget.create(USER_ID, CATEGORY_ID, PERIOD_JULY_2026, new Money(new BigDecimal("100000.00"), ARS), null);
        Money spend = new Money(new BigDecimal("10000.00"), ARS);

        BudgetPaceResult startResult = budgetPace.evaluate(budget, spend, LocalDate.of(2026, 7, 1));
        assertThat(startResult.expectedPctByToday()).isEqualTo(new BigDecimal("3.23")); // 1/31 * 100

        BudgetPaceResult endResult = budgetPace.evaluate(budget, spend, LocalDate.of(2026, 7, 31));
        assertThat(endResult.expectedPctByToday()).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    void evaluatesOverBudgetBoundaries() {
        Budget budget = Budget.create(USER_ID, CATEGORY_ID, PERIOD_JULY_2026, new Money(new BigDecimal("50000.00"), ARS), null);

        // spend == amount -> overBudget false, remaining null
        Money exactSpend = new Money(new BigDecimal("50000.00"), ARS);
        BudgetPaceResult exactResult = budgetPace.evaluate(budget, exactSpend, LocalDate.of(2026, 7, 15));
        assertThat(exactResult.overBudget()).isFalse();
        assertThat(exactResult.remaining()).isNull();
        assertThat(exactResult.pctUsed()).isEqualTo(new BigDecimal("100.00"));

        // spend > amount -> overBudget true, remaining null
        Money overSpend = new Money(new BigDecimal("60000.00"), ARS);
        BudgetPaceResult overResult = budgetPace.evaluate(budget, overSpend, LocalDate.of(2026, 7, 15));
        assertThat(overResult.overBudget()).isTrue();
        assertThat(overResult.remaining()).isNull();
        assertThat(overResult.pctUsed()).isEqualTo(new BigDecimal("120.00"));
    }

    @Test
    void handlesDateOutsidePeriod() {
        Budget budget = Budget.create(USER_ID, CATEGORY_ID, PERIOD_JULY_2026, new Money(new BigDecimal("50000.00"), ARS), null);
        Money spend = new Money(new BigDecimal("10000.00"), ARS);

        // Before period
        BudgetPaceResult before = budgetPace.evaluate(budget, spend, LocalDate.of(2026, 6, 30));
        assertThat(before.expectedPctByToday()).isEqualTo(new BigDecimal("0.00"));

        // After period
        BudgetPaceResult after = budgetPace.evaluate(budget, spend, LocalDate.of(2026, 8, 1));
        assertThat(after.expectedPctByToday()).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    void rejectsCurrencyMismatch() {
        Budget budget = Budget.create(USER_ID, CATEGORY_ID, PERIOD_JULY_2026, new Money(new BigDecimal("50000.00"), ARS), null);
        Money usdSpend = new Money(new BigDecimal("100.00"), USD);

        assertThatThrownBy(() -> budgetPace.evaluate(budget, usdSpend, LocalDate.of(2026, 7, 15)))
                .isInstanceOf(InvalidBudgetException.class)
                .hasMessageContaining("Currency mismatch");
    }
}
