package com.financialapp.finances.web.controller;

import com.financialapp.finances.domain.common.model.*;
import com.financialapp.finances.domain.gateway.AccountOwnershipGateway;
import com.financialapp.finances.domain.model.transaction.Transaction;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TransactionWebMapper.class)
class AccountTransactionContractIT {

    @Autowired MockMvc mvc;
    @MockBean RecordTransaction recordTransaction;
    @MockBean UpdateTransaction updateTransaction;
    @MockBean DeleteTransaction deleteTransaction;
    @MockBean ListUserTransactions listUserTransactions;
    @MockBean GetTransactionSummary getTransactionSummary;
    @MockBean ListAccountTransactions listAccountTransactions;
    @MockBean TransactionClassifier classifier;
    @MockBean AccountOwnershipGateway ownershipGateway;

    @Test
    void jsonExposesExactlyTheFieldsMsBanksTransactionDtoExpects() throws Exception {
        Currency ARS = Currency.getInstance("ARS");
        Cbu mine = new Cbu("0001112223334445556667");
        Transaction tx = Transaction.reconstitute(new TransactionId(77L), new UserId(42L),
                mine, new Cbu("9998887776665554443332"),
                new Money(new BigDecimal("100.00"), ARS), new CategoryId(5L), "Rent", LocalDate.of(2026, 6, 1));
        when(listAccountTransactions.execute(eq(mine), any(), any(), any()))
                .thenReturn(List.of(new AccountTransactionView(tx, new CategoryNames("Housing", "Rent"))));

        mvc.perform(get("/api/v1/finances/transactions").param("accountCbu", "0001112223334445556667"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].transactionId").exists())
                .andExpect(jsonPath("$.data[0].accountCbu").exists())
                .andExpect(jsonPath("$.data[0].amount").exists())
                .andExpect(jsonPath("$.data[0].currency").value("ARS"))
                .andExpect(jsonPath("$.data[0].description").value("Rent"))
                .andExpect(jsonPath("$.data[0].category").value("Housing"))
                .andExpect(jsonPath("$.data[0].subcategory").value("Rent"))
                .andExpect(jsonPath("$.data[0].date").value("2026-06-01"));
    }
}
