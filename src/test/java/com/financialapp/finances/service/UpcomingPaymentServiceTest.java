package com.financialapp.finances.service;

import com.financialapp.finances.model.dto.response.UpcomingPaymentResponse;
import com.financialapp.finances.model.entity.CardExpense;
import com.financialapp.finances.model.entity.CardExpenseInstallment;
import com.financialapp.finances.model.entity.Loan;
import com.financialapp.finances.model.entity.LoanInstallment;
import com.financialapp.finances.repository.CardExpenseInstallmentRepository;
import com.financialapp.finances.repository.LoanInstallmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpcomingPaymentService")
class UpcomingPaymentServiceTest {

    @Mock private LoanInstallmentRepository loanInstallmentRepository;
    @Mock private CardExpenseInstallmentRepository cardExpenseInstallmentRepository;

    @InjectMocks private UpcomingPaymentService upcomingPaymentService;

    private static final Long USER_ID = 1L;

    private Loan buildLoan(Long id, String description, String currency) {
        return Loan.builder()
                .id(id)
                .userId(USER_ID)
                .description(description)
                .currency(currency)
                .totalInstallments(12)
                .build();
    }

    private CardExpense buildCardExpense(Long id, String description, String currency) {
        return CardExpense.builder()
                .id(id)
                .userId(USER_ID)
                .description(description)
                .currency(currency)
                .totalInstallments(6)
                .build();
    }

    @Test
    @DisplayName("merges loan and card expense installments into a single sorted list")
    void mergesAndSortsByDueDate() {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 1, 31);

        Loan loan = buildLoan(1L, "Préstamo auto", "ARS");
        LoanInstallment li = LoanInstallment.builder()
                .id(10L)
                .loan(loan)
                .installmentNumber(3)
                .amount(new BigDecimal("15000"))
                .dueDate(LocalDate.of(2025, 1, 20))
                .paid(false)
                .build();

        CardExpense ce = buildCardExpense(2L, "Heladera", "ARS");
        CardExpenseInstallment cei = CardExpenseInstallment.builder()
                .id(20L)
                .cardExpense(ce)
                .installmentNumber(1)
                .amount(new BigDecimal("10000"))
                .dueDate(LocalDate.of(2025, 1, 5))
                .paid(false)
                .build();

        when(loanInstallmentRepository.findUpcomingUnpaidByUser(USER_ID, from, to, null))
                .thenReturn(List.of(li));
        when(cardExpenseInstallmentRepository.findUpcomingUnpaidByUser(USER_ID, from, to, null))
                .thenReturn(List.of(cei));

        List<UpcomingPaymentResponse> result = upcomingPaymentService.getUpcomingPayments(USER_ID, from, to, null);

        assertThat(result).hasSize(2);
        // sorted by dueDate: card expense on Jan 5 comes first
        assertThat(result.get(0).getType()).isEqualTo("CARD_EXPENSE");
        assertThat(result.get(0).getDueDate()).isEqualTo(LocalDate.of(2025, 1, 5));
        assertThat(result.get(1).getType()).isEqualTo("LOAN");
        assertThat(result.get(1).getDueDate()).isEqualTo(LocalDate.of(2025, 1, 20));
    }

    @Test
    @DisplayName("maps loan installment fields correctly")
    void mapsLoanInstallmentFields() {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 1, 31);

        Loan loan = buildLoan(5L, "Préstamo vivienda", "USD");
        LoanInstallment li = LoanInstallment.builder()
                .id(10L)
                .loan(loan)
                .installmentNumber(7)
                .amount(new BigDecimal("500"))
                .dueDate(LocalDate.of(2025, 1, 10))
                .paid(false)
                .build();

        when(loanInstallmentRepository.findUpcomingUnpaidByUser(USER_ID, from, to, "USD"))
                .thenReturn(List.of(li));
        when(cardExpenseInstallmentRepository.findUpcomingUnpaidByUser(USER_ID, from, to, "USD"))
                .thenReturn(List.of());

        List<UpcomingPaymentResponse> result = upcomingPaymentService.getUpcomingPayments(USER_ID, from, to, "USD");

        assertThat(result).hasSize(1);
        UpcomingPaymentResponse resp = result.get(0);
        assertThat(resp.getSourceId()).isEqualTo(5L);
        assertThat(resp.getType()).isEqualTo("LOAN");
        assertThat(resp.getDescription()).isEqualTo("Préstamo vivienda");
        assertThat(resp.getAmount()).isEqualByComparingTo("500");
        assertThat(resp.getCurrency()).isEqualTo("USD");
        assertThat(resp.getInstallmentNumber()).isEqualTo(7);
        assertThat(resp.getTotalInstallments()).isEqualTo(12);
        assertThat(resp.isPaid()).isFalse();
    }

    @Test
    @DisplayName("maps card expense installment fields correctly")
    void mapsCardExpenseInstallmentFields() {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 1, 31);

        CardExpense ce = buildCardExpense(3L, "Notebook", "USD");
        CardExpenseInstallment cei = CardExpenseInstallment.builder()
                .id(30L)
                .cardExpense(ce)
                .installmentNumber(2)
                .amount(new BigDecimal("200"))
                .dueDate(LocalDate.of(2025, 1, 15))
                .paid(false)
                .build();

        when(loanInstallmentRepository.findUpcomingUnpaidByUser(USER_ID, from, to, null))
                .thenReturn(List.of());
        when(cardExpenseInstallmentRepository.findUpcomingUnpaidByUser(USER_ID, from, to, null))
                .thenReturn(List.of(cei));

        List<UpcomingPaymentResponse> result = upcomingPaymentService.getUpcomingPayments(USER_ID, from, to, null);

        assertThat(result).hasSize(1);
        UpcomingPaymentResponse resp = result.get(0);
        assertThat(resp.getSourceId()).isEqualTo(3L);
        assertThat(resp.getType()).isEqualTo("CARD_EXPENSE");
        assertThat(resp.getDescription()).isEqualTo("Notebook");
        assertThat(resp.getInstallmentNumber()).isEqualTo(2);
        assertThat(resp.getTotalInstallments()).isEqualTo(6);
    }

    @Test
    @DisplayName("returns empty list when no upcoming payments")
    void returnsEmptyList() {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 1, 31);

        when(loanInstallmentRepository.findUpcomingUnpaidByUser(USER_ID, from, to, null))
                .thenReturn(List.of());
        when(cardExpenseInstallmentRepository.findUpcomingUnpaidByUser(USER_ID, from, to, null))
                .thenReturn(List.of());

        List<UpcomingPaymentResponse> result = upcomingPaymentService.getUpcomingPayments(USER_ID, from, to, null);

        assertThat(result).isEmpty();
    }
}
