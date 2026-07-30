package com.financialapp.finances.domain.model.budget;

import com.financialapp.finances.domain.common.model.BudgetId;
import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.Money;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.exception.budget.InvalidBudgetException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BudgetTest {

    private static final UserId USER_ID = new UserId(1L);
    private static final CategoryId CATEGORY_ID = new CategoryId(10L);
    private static final BudgetPeriod PERIOD = new BudgetPeriod(2026, 7);
    private static final Money AMOUNT = new Money(new BigDecimal("50000.00"), Currency.getInstance("ARS"));

    @Test
    void createsValidBudgetWithoutThreshold() {
        Budget budget = Budget.create(USER_ID, CATEGORY_ID, PERIOD, AMOUNT, null);

        assertThat(budget.id()).isNull();
        assertThat(budget.userId()).isEqualTo(USER_ID);
        assertThat(budget.categoryId()).isEqualTo(CATEGORY_ID);
        assertThat(budget.period()).isEqualTo(PERIOD);
        assertThat(budget.amount()).isEqualTo(AMOUNT);
        assertThat(budget.alertThresholdPct()).isNull();
        assertThat(budget.lastAlertedPeriod()).isNull();
    }

    @Test
    void createsValidBudgetWithThreshold() {
        BigDecimal threshold = new BigDecimal("90.00");
        Budget budget = Budget.create(USER_ID, CATEGORY_ID, PERIOD, AMOUNT, threshold);

        assertThat(budget.alertThresholdPct()).isEqualTo(threshold);
    }

    @Test
    void rejectsNullRequiredFields() {
        assertThatThrownBy(() -> Budget.create(null, CATEGORY_ID, PERIOD, AMOUNT, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Budget.create(USER_ID, null, PERIOD, AMOUNT, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Budget.create(USER_ID, CATEGORY_ID, null, AMOUNT, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Budget.create(USER_ID, CATEGORY_ID, PERIOD, null, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsInvalidAlertThresholds() {
        assertThatThrownBy(() -> Budget.create(USER_ID, CATEGORY_ID, PERIOD, AMOUNT, BigDecimal.ZERO))
                .isInstanceOf(InvalidBudgetException.class);
        assertThatThrownBy(() -> Budget.create(USER_ID, CATEGORY_ID, PERIOD, AMOUNT, new BigDecimal("-1.00")))
                .isInstanceOf(InvalidBudgetException.class);
        assertThatThrownBy(() -> Budget.create(USER_ID, CATEGORY_ID, PERIOD, AMOUNT, new BigDecimal("100.01")))
                .isInstanceOf(InvalidBudgetException.class);
    }

    @Test
    void markAlertedReturnsNewInstanceLeavingOriginalUntouched() {
        Budget original = Budget.create(USER_ID, CATEGORY_ID, PERIOD, AMOUNT, new BigDecimal("90.00"));
        BudgetPeriod alertPeriod = new BudgetPeriod(2026, 7);

        Budget alerted = original.markAlerted(alertPeriod);

        assertThat(alerted).isNotSameAs(original);
        assertThat(original.lastAlertedPeriod()).isNull();
        assertThat(alerted.lastAlertedPeriod()).isEqualTo(alertPeriod);
        assertThat(alerted.userId()).isEqualTo(original.userId());
        assertThat(alerted.categoryId()).isEqualTo(original.categoryId());
        assertThat(alerted.period()).isEqualTo(original.period());
        assertThat(alerted.amount()).isEqualTo(original.amount());
        assertThat(alerted.alertThresholdPct()).isEqualTo(original.alertThresholdPct());
    }

    @Test
    void reconstitutesBudgetWithAllFields() {
        BudgetId budgetId = new BudgetId(100L);
        BudgetPeriod lastAlerted = new BudgetPeriod(2026, 6);
        BigDecimal threshold = new BigDecimal("80.00");

        Budget budget = Budget.reconstitute(budgetId, USER_ID, CATEGORY_ID, PERIOD, AMOUNT, threshold, lastAlerted);

        assertThat(budget.id()).isEqualTo(budgetId);
        assertThat(budget.lastAlertedPeriod()).isEqualTo(lastAlerted);
    }
}
