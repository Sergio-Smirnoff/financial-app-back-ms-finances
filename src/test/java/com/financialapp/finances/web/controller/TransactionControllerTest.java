package com.financialapp.finances.web.controller;
import com.financialapp.commons.core.domain.model.Cbu;

import com.financialapp.commons.core.domain.model.PageResult;
import com.financialapp.finances.domain.common.model.*;
import com.financialapp.finances.domain.gateway.AccountOwnershipGateway;
import com.financialapp.finances.domain.model.category.CategoryNames;
import com.financialapp.finances.domain.usecase.transaction.AccountTransactionView;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.model.transaction.TransactionKind;
import com.financialapp.finances.domain.model.transaction.TransactionSummary;
import com.financialapp.finances.domain.repository.CategoryRepository;
import com.financialapp.finances.domain.service.TransactionClassifier;
import com.financialapp.finances.domain.usecase.transaction.*;
import com.financialapp.finances.web.mapper.TransactionWebMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TransactionWebMapper.class)
class TransactionControllerTest {

    @Autowired MockMvc mvc;
    @MockBean RecordTransaction recordTransaction;
    @MockBean UpdateTransaction updateTransaction;
    @MockBean DeleteTransaction deleteTransaction;
    @MockBean GetTransactionSummary getTransactionSummary;
    @MockBean ListAccountTransactions listAccountTransactions;
    @MockBean ListTransactionsFiltered listTransactionsFiltered;
    @MockBean CountUncategorisedTransactions countUncategorisedTransactions;
    @MockBean GetTransactionDetail getTransactionDetail;
    @MockBean SearchTransactions searchTransactions;
    @MockBean GetMonthlyFlow getMonthlyFlow;
    @MockBean CategoryRepository categoryRepository;
    @MockBean TransactionClassifier classifier;
    @MockBean AccountOwnershipGateway ownershipGateway;

    private static final Currency ARS = Currency.getInstance("ARS");

    @Test
    void accountScopedListReturnsMsBanksDtoShape() throws Exception {
        Cbu mine = new Cbu("0001112223334445556667");
        Transaction tx = Transaction.reconstitute(new TransactionId(77L), new UserId(42L),
                mine, new Cbu("9998887776665554443332"),
                new Money(new BigDecimal("100.00"), ARS), new CategoryId(5L), "Rent", LocalDate.of(2026, 6, 1));
        when(listAccountTransactions.execute(eq(mine), any(), any(), any()))
                .thenReturn(List.of(new AccountTransactionView(tx, new CategoryNames("Housing", "Rent"))));

        mvc.perform(get("/api/v1/finances/transactions")
                        .param("accountCbu", "0001112223334445556667"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].transactionId").value(77))
                .andExpect(jsonPath("$.data[0].accountCbu").value("0001112223334445556667"))
                .andExpect(jsonPath("$.data[0].amount").value("-100.00"))
                .andExpect(jsonPath("$.data[0].category").value("Housing"))
                .andExpect(jsonPath("$.data[0].subcategory").value("Rent"));
    }

    @Test
    void userFilteredListReturnsPageResult() throws Exception {
        Cbu from = new Cbu("0001112223334445556667");
        Cbu to = new Cbu("9998887776665554443332");
        Transaction tx = Transaction.reconstitute(new TransactionId(77L), new UserId(42L),
                from, to, new Money(new BigDecimal("100.00"), ARS), new CategoryId(5L), "Supermarket", LocalDate.of(2026, 6, 1));

        when(listTransactionsFiltered.execute(any()))
                .thenReturn(new PageResult<>(List.of(tx), true, "next_cursor_str", 100L));
        when(ownershipGateway.ownedAccounts(new UserId(42L))).thenReturn(Set.of(from));
        when(classifier.classify(eq(tx), any())).thenReturn(TransactionKind.EXPENSE);
        when(categoryRepository.findNamesById(new CategoryId(5L))).thenReturn(Optional.of(new CategoryNames("Food", "Supermarket")));

        mvc.perform(get("/api/v1/finances/transactions")
                        .header("X-User-Id", 42L)
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(77))
                .andExpect(jsonPath("$.data.content[0].kind").value("EXPENSE"))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.nextCursor").value("next_cursor_str"))
                .andExpect(jsonPath("$.data.totalElements").value(100));
    }

    @Test
    void countUncategorisedReturnsCount() throws Exception {
        when(countUncategorisedTransactions.execute(new UserId(42L))).thenReturn(7L);

        mvc.perform(get("/api/v1/finances/transactions/uncategorised/count")
                        .header("X-User-Id", 42L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(7));
    }

    @Test
    void summaryWithRangeReturnsPerCurrencyMap() throws Exception {
        when(getTransactionSummary.execute(any(UserId.class), any(DateRange.class)))
                .thenReturn(List.of(new TransactionSummary(ARS,
                        new BigDecimal("100"), new BigDecimal("40"), new BigDecimal("60"))));

        mvc.perform(get("/api/v1/finances/transactions/summary")
                        .header("X-User-Id", 1L)
                        .param("from", "2026-05-01").param("to", "2026-05-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.ARS.balance").value("60"));
    }

    @Test
    void summaryWithOnlyOneBoundIsBadRequest() throws Exception {
        mvc.perform(get("/api/v1/finances/transactions/summary")
                        .header("X-User-Id", 1L)
                        .param("from", "2026-05-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchReturnsMatchingTransactions() throws Exception {
        Cbu from = new Cbu("0001112223334445556667");
        Cbu to = new Cbu("9998887776665554443332");
        Transaction tx = Transaction.reconstitute(new TransactionId(1L), new UserId(7L),
                from, to, new Money(new BigDecimal("12500.00"), ARS), new CategoryId(2L), "Supermercado", LocalDate.of(2026, 7, 4));

        when(searchTransactions.execute(new UserId(7L), "super", 10)).thenReturn(List.of(tx));
        when(ownershipGateway.ownedAccounts(new UserId(7L))).thenReturn(Set.of(from));
        when(classifier.classify(eq(tx), any())).thenReturn(TransactionKind.EXPENSE);

        mvc.perform(get("/api/v1/finances/transactions/search")
                        .header("X-User-Id", 7L)
                        .param("q", "super")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].description").value("Supermercado"))
                .andExpect(jsonPath("$.data[0].amount").value("12500.00"))
                .andExpect(jsonPath("$.data[0].currency").value("ARS"))
                .andExpect(jsonPath("$.data[0].direction").value("OUT"));
    }

    @Test
    void monthlySummaryReturnsFlowSeries() throws Exception {
        com.financialapp.finances.domain.model.transaction.MonthlyFlow flow =
                new com.financialapp.finances.domain.model.transaction.MonthlyFlow(
                        java.time.YearMonth.of(2026, 7), ARS, new BigDecimal("150000.00"), new BigDecimal("85000.00"));

        when(getMonthlyFlow.execute(eq(new UserId(7L)), any(DateRange.class)))
                .thenReturn(List.of(flow));

        mvc.perform(get("/api/v1/finances/transactions/summary/monthly")
                        .header("X-User-Id", 7L)
                        .param("from", "2026-07-01")
                        .param("to", "2026-07-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].month").value("2026-07"))
                .andExpect(jsonPath("$.data[0].currency").value("ARS"))
                .andExpect(jsonPath("$.data[0].income").value("150000.00"))
                .andExpect(jsonPath("$.data[0].expense").value("85000.00"));
    }
}
