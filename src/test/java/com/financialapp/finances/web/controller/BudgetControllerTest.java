package com.financialapp.finances.web.controller;

import com.financialapp.finances.domain.common.model.BudgetId;
import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.Money;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.budget.Budget;
import com.financialapp.finances.domain.model.budget.BudgetPeriod;
import com.financialapp.finances.domain.service.BudgetPaceResult;
import com.financialapp.finances.domain.usecase.budget.BudgetPaceView;
import com.financialapp.finances.domain.usecase.budget.BudgetView;
import com.financialapp.finances.domain.usecase.budget.GetBudgetPace;
import com.financialapp.finances.domain.usecase.budget.GetBudgets;
import com.financialapp.finances.domain.usecase.budget.UpsertBudget;
import com.financialapp.finances.web.mapper.BudgetWebMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BudgetController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(BudgetWebMapper.class)
class BudgetControllerTest {

    @Autowired MockMvc mvc;
    @MockBean UpsertBudget upsertBudget;
    @MockBean GetBudgets getBudgets;
    @MockBean GetBudgetPace getBudgetPace;

    private static final Currency ARS = Currency.getInstance("ARS");

    @Test
    void upsertReturns200WithBudgetResponse() throws Exception {
        Budget b = Budget.reconstitute(new BudgetId(1L), new UserId(42L), new CategoryId(10L),
                new BudgetPeriod(2026, 7), new Money(new BigDecimal("50000.00"), ARS), new BigDecimal("90.00"), null);
        when(upsertBudget.execute(any())).thenReturn(new BudgetView(b, "Food"));

        mvc.perform(put("/api/v1/finances/budgets/10")
                        .header("X-User-Id", "42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":\"50000.00\",\"currency\":\"ARS\",\"alertThresholdPct\":\"90\",\"year\":2026,\"month\":7}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.categoryId").value(10))
                .andExpect(jsonPath("$.data.categoryName").value("Food"))
                .andExpect(jsonPath("$.data.amount").value("50000.00"))
                .andExpect(jsonPath("$.data.currency").value("ARS"))
                .andExpect(jsonPath("$.data.alertThresholdPct").value("90.00"));
    }

    @Test
    void getBudgetsReturnsList() throws Exception {
        Budget b = Budget.reconstitute(new BudgetId(1L), new UserId(42L), new CategoryId(10L),
                new BudgetPeriod(2026, 7), new Money(new BigDecimal("50000.00"), ARS), null, null);
        when(getBudgets.execute(any(), any())).thenReturn(List.of(new BudgetView(b, "Food")));

        mvc.perform(get("/api/v1/finances/budgets?year=2026&month=7")
                        .header("X-User-Id", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].categoryId").value(10))
                .andExpect(jsonPath("$.data[0].amount").value("50000.00"));
    }

    @Test
    void getBudgetPaceReturnsPaceList() throws Exception {
        Budget b = Budget.reconstitute(new BudgetId(1L), new UserId(42L), new CategoryId(10L),
                new BudgetPeriod(2026, 7), new Money(new BigDecimal("100000.00"), ARS), null, null);
        BudgetPaceResult pace = new BudgetPaceResult(
                new Money(new BigDecimal("30000.00"), ARS),
                new Money(new BigDecimal("70000.00"), ARS),
                new BigDecimal("30.00"),
                new BigDecimal("48.39"),
                false
        );
        when(getBudgetPace.execute(any(), any(), any())).thenReturn(List.of(new BudgetPaceView(b, "Food", pace)));

        mvc.perform(get("/api/v1/finances/budgets/pace?year=2026&month=7")
                        .header("X-User-Id", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].categoryId").value(10))
                .andExpect(jsonPath("$.data[0].spent").value("30000.00"))
                .andExpect(jsonPath("$.data[0].remaining").value("70000.00"))
                .andExpect(jsonPath("$.data[0].pctUsed").value("30.00"))
                .andExpect(jsonPath("$.data[0].expectedPctByToday").value("48.39"))
                .andExpect(jsonPath("$.data[0].overBudget").value(false));
    }
}
