package com.financialapp.finances.web.controller;

import com.financialapp.finances.domain.common.model.*;
import com.financialapp.finances.domain.gateway.AccountOwnershipGateway;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.model.transaction.TransactionKind;
import com.financialapp.finances.domain.model.transaction.TransactionSummary;
import com.financialapp.finances.domain.service.TransactionClassifier;
import com.financialapp.finances.domain.usecase.transaction.DeleteTransaction;
import com.financialapp.finances.domain.usecase.transaction.GetTransactionSummary;
import com.financialapp.finances.domain.usecase.transaction.ListAccountTransactions;
import com.financialapp.finances.domain.usecase.transaction.ListUserTransactions;
import com.financialapp.finances.domain.usecase.transaction.RecordTransaction;
import com.financialapp.finances.domain.usecase.transaction.UpdateTransaction;
import com.financialapp.finances.domain.model.category.CategoryNames;
import com.financialapp.finances.domain.usecase.transaction.AccountTransactionView;
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
import java.util.List;
import java.util.Currency;

import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TransactionWebMapper.class)
class TransactionControllerIT {

    @Autowired MockMvc mvc;
    @MockBean RecordTransaction recordTransaction;
    @MockBean UpdateTransaction updateTransaction;
    @MockBean DeleteTransaction deleteTransaction;
    @MockBean ListUserTransactions listUserTransactions;
    @MockBean GetTransactionSummary getTransactionSummary;
    @MockBean ListAccountTransactions listAccountTransactions;
    @MockBean TransactionClassifier classifier;
    @MockBean AccountOwnershipGateway ownershipGateway;
    @MockBean com.financialapp.finances.domain.gateway.SupportedCurrencies supportedCurrencies;

    private static final Currency ARS = Currency.getInstance("ARS");

    @org.junit.jupiter.api.BeforeEach
    void supportAllCurrencies() {
        when(supportedCurrencies.isSupported(any())).thenReturn(true);
    }

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
    void summaryWithRangeReturnsPerCurrencyMap() throws Exception {
        when(getTransactionSummary.execute(any(UserId.class), any(DateRange.class)))
                .thenReturn(List.of(new TransactionSummary(ARS,
                        new BigDecimal("100"), new BigDecimal("40"), new BigDecimal("60"))));

        mvc.perform(get("/api/v1/finances/transactions/summary")
                        .header("X-User-Id", 1L)
                        .param("from", "2026-05-01").param("to", "2026-05-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.ARS.balance").value("60"));
    }

    @Test
    void summaryWithOnlyOneBoundIsBadRequest() throws Exception {
        mvc.perform(get("/api/v1/finances/transactions/summary")
                        .header("X-User-Id", 1L)
                        .param("from", "2026-05-01"))
                .andExpect(status().isBadRequest());
    }

    private Transaction savedTx() {
        return Transaction.reconstitute(new TransactionId(77L), new UserId(42L),
                new Cbu("0001112223334445556667"), new Cbu("9998887776665554443332"),
                new Money(new BigDecimal("100.00"), ARS), new CategoryId(5L), "Rent", LocalDate.of(2026, 6, 1));
    }

    @Test
    void recordReturns201WithClassifiedTransaction() throws Exception {
        when(recordTransaction.execute(any())).thenReturn(savedTx());
        when(ownershipGateway.ownedAccounts(new UserId(42L)))
                .thenReturn(java.util.Set.of(new Cbu("0001112223334445556667")));
        when(classifier.classify(any(), any())).thenReturn(TransactionKind.EXPENSE);

        mvc.perform(post("/api/v1/finances/transactions").header("X-User-Id", "42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromCbu\":\"0001112223334445556667\",\"toCbu\":\"9998887776665554443332\","
                                + "\"amount\":\"100.00\",\"currency\":\"ARS\",\"categoryId\":5,"
                                + "\"description\":\"Rent\",\"date\":\"2026-06-01\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(77))
                .andExpect(jsonPath("$.data.kind").value("EXPENSE"));
    }

    @Test
    void updateReturnsClassifiedTransaction() throws Exception {
        when(updateTransaction.execute(any())).thenReturn(savedTx());
        when(ownershipGateway.ownedAccounts(new UserId(42L)))
                .thenReturn(java.util.Set.of(new Cbu("9998887776665554443332")));
        when(classifier.classify(any(), any())).thenReturn(TransactionKind.INCOME);

        mvc.perform(put("/api/v1/finances/transactions/77").header("X-User-Id", "42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":9,\"description\":\"Updated\",\"date\":\"2026-06-02\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.kind").value("INCOME"));
    }

    @Test
    void deleteReturnsSuccess() throws Exception {
        mvc.perform(delete("/api/v1/finances/transactions/77").header("X-User-Id", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        verify(deleteTransaction).execute(any());
    }

    @Test
    void userScopedListReturnsUserTransactions() throws Exception {
        when(listUserTransactions.execute(new UserId(42L)))
                .thenReturn(List.of(new com.financialapp.finances.domain.model.transaction.ClassifiedTransaction(
                        savedTx(), TransactionKind.EXPENSE)));
        mvc.perform(get("/api/v1/finances/transactions").header("X-User-Id", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(77))
                .andExpect(jsonPath("$.data[0].kind").value("EXPENSE"));
    }

    @Test
    void listWithoutUserOrAccountIsBadRequest() throws Exception {
        // Neither accountCbu nor X-User-Id present -> ConstraintViolation -> 400
        mvc.perform(get("/api/v1/finances/transactions"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void allTimeSummaryUsesNoDateRange() throws Exception {
        when(getTransactionSummary.execute(any(UserId.class)))
                .thenReturn(List.of(new TransactionSummary(ARS,
                        new BigDecimal("100"), new BigDecimal("40"), new BigDecimal("60"))));
        mvc.perform(get("/api/v1/finances/transactions/summary").header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ARS.balance").value("60"));
    }

    @Test
    void summaryMergesDuplicateCurrencyKeepingFirst() throws Exception {
        // Two summaries for the same currency exercise the toMap merge function (a, b) -> a.
        when(getTransactionSummary.execute(any(UserId.class)))
                .thenReturn(List.of(
                        new TransactionSummary(ARS, new BigDecimal("100"), new BigDecimal("40"), new BigDecimal("60")),
                        new TransactionSummary(ARS, new BigDecimal("1"), new BigDecimal("1"), new BigDecimal("0"))));
        mvc.perform(get("/api/v1/finances/transactions/summary").header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ARS.balance").value("60")); // first kept
    }

    @Test
    void updateWithNullCategoryLeavesCategoryUnchanged() throws Exception {
        when(updateTransaction.execute(any())).thenReturn(savedTx());
        when(ownershipGateway.ownedAccounts(new UserId(42L)))
                .thenReturn(java.util.Set.of(new Cbu("0001112223334445556667")));
        when(classifier.classify(any(), any())).thenReturn(TransactionKind.EXPENSE);

        // categoryId omitted (null) -> the `req.categoryId() != null ? ... : null` false branch
        mvc.perform(put("/api/v1/finances/transactions/77").header("X-User-Id", "42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Updated\",\"date\":\"2026-06-02\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.kind").value("EXPENSE"));
    }
}
