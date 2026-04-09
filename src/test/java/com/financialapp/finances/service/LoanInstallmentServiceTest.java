package com.financialapp.finances.service;

import com.financialapp.finances.exception.BusinessException;
import com.financialapp.finances.exception.ResourceNotFoundException;
import com.financialapp.finances.mapper.LoanMapper;
import com.financialapp.finances.model.dto.response.LoanInstallmentResponse;
import com.financialapp.finances.model.entity.Loan;
import com.financialapp.finances.model.entity.LoanInstallment;
import com.financialapp.finances.repository.LoanInstallmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoanInstallmentService")
class LoanInstallmentServiceTest {

    @Mock private LoanInstallmentRepository installmentRepository;
    @Mock private LoanService loanService;
    @Mock private LoanMapper loanMapper;

    @InjectMocks private LoanInstallmentService loanInstallmentService;

    private static final Long USER_ID = 1L;
    private static final Long LOAN_ID = 10L;

    private Loan buildActiveLoan() {
        return Loan.builder()
                .id(LOAN_ID)
                .userId(USER_ID)
                .description("Préstamo")
                .totalAmount(new BigDecimal("30000"))
                .currency("ARS")
                .totalInstallments(3)
                .paidInstallments(0)
                .installmentAmount(new BigDecimal("10000"))
                .active(true)
                .build();
    }

    private LoanInstallment buildInstallment(Long id, int number, boolean paid, Loan loan) {
        return LoanInstallment.builder()
                .id(id)
                .loan(loan)
                .installmentNumber(number)
                .amount(new BigDecimal("10000"))
                .dueDate(LocalDate.of(2025, number, 1))
                .paid(paid)
                .build();
    }

    @Nested
    @DisplayName("payInstallment")
    class PayInstallment {

        @Test
        @DisplayName("marks installment as paid when no previous unpaid installments")
        void paysFirstInstallment() {
            Loan loan = buildActiveLoan();
            LoanInstallment inst1 = buildInstallment(1L, 1, false, loan);
            LoanInstallment inst2 = buildInstallment(2L, 2, false, loan);
            LoanInstallment inst3 = buildInstallment(3L, 3, false, loan);

            LoanInstallmentResponse resp = LoanInstallmentResponse.builder()
                    .id(1L).installmentNumber(1).paid(true).build();

            when(loanService.findOwnedLoan(LOAN_ID, USER_ID)).thenReturn(loan);
            when(installmentRepository.findById(1L)).thenReturn(Optional.of(inst1));
            when(installmentRepository.findByLoanIdOrderByInstallmentNumberAsc(LOAN_ID))
                    .thenReturn(List.of(inst1, inst2, inst3));
            when(installmentRepository.save(inst1)).thenReturn(inst1);
            when(loanMapper.toInstallmentResponse(inst1)).thenReturn(resp);

            LoanInstallmentResponse result = loanInstallmentService.payInstallment(LOAN_ID, 1L, USER_ID);

            assertThat(result.isPaid()).isTrue();
            assertThat(inst1.isPaid()).isTrue();
            assertThat(inst1.getPaidDate()).isNotNull();
            assertThat(loan.getPaidInstallments()).isEqualTo(1);
            verify(loanService).updateNextPaymentDate(loan);
            verify(loanService).markLoanClosedIfFullyPaid(loan);
        }

        @Test
        @DisplayName("throws BusinessException when trying to pay installment #2 with installment #1 unpaid")
        void throwsWhenPreviousInstallmentUnpaid() {
            Loan loan = buildActiveLoan();
            LoanInstallment inst1 = buildInstallment(1L, 1, false, loan);
            LoanInstallment inst2 = buildInstallment(2L, 2, false, loan);

            when(loanService.findOwnedLoan(LOAN_ID, USER_ID)).thenReturn(loan);
            when(installmentRepository.findById(2L)).thenReturn(Optional.of(inst2));
            when(installmentRepository.findByLoanIdOrderByInstallmentNumberAsc(LOAN_ID))
                    .thenReturn(List.of(inst1, inst2));

            assertThatThrownBy(() -> loanInstallmentService.payInstallment(LOAN_ID, 2L, USER_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("installment #1 is still unpaid");

            verify(installmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws BusinessException when installment is already paid")
        void throwsWhenAlreadyPaid() {
            Loan loan = buildActiveLoan();
            LoanInstallment paid = buildInstallment(1L, 1, true, loan);

            when(loanService.findOwnedLoan(LOAN_ID, USER_ID)).thenReturn(loan);
            when(installmentRepository.findById(1L)).thenReturn(Optional.of(paid));

            assertThatThrownBy(() -> loanInstallmentService.payInstallment(LOAN_ID, 1L, USER_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already been paid");

            verify(installmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws BusinessException when loan is inactive")
        void throwsWhenLoanInactive() {
            Loan loan = buildActiveLoan();
            loan.setActive(false);

            when(loanService.findOwnedLoan(LOAN_ID, USER_ID)).thenReturn(loan);

            assertThatThrownBy(() -> loanInstallmentService.payInstallment(LOAN_ID, 1L, USER_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("inactive loan");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when installment belongs to different loan")
        void throwsWhenInstallmentNotBelongsToLoan() {
            Loan loan = buildActiveLoan();
            Loan otherLoan = buildActiveLoan();
            otherLoan.setId(99L);
            LoanInstallment inst = buildInstallment(1L, 1, false, otherLoan);

            when(loanService.findOwnedLoan(LOAN_ID, USER_ID)).thenReturn(loan);
            when(installmentRepository.findById(1L)).thenReturn(Optional.of(inst));

            assertThatThrownBy(() -> loanInstallmentService.payInstallment(LOAN_ID, 1L, USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("can pay installment #2 when installment #1 is already paid")
        void paysSecondInstallmentWhenFirstPaid() {
            Loan loan = buildActiveLoan();
            loan.setPaidInstallments(1);
            LoanInstallment inst1 = buildInstallment(1L, 1, true, loan);
            LoanInstallment inst2 = buildInstallment(2L, 2, false, loan);

            LoanInstallmentResponse resp = LoanInstallmentResponse.builder()
                    .id(2L).installmentNumber(2).paid(true).build();

            when(loanService.findOwnedLoan(LOAN_ID, USER_ID)).thenReturn(loan);
            when(installmentRepository.findById(2L)).thenReturn(Optional.of(inst2));
            when(installmentRepository.findByLoanIdOrderByInstallmentNumberAsc(LOAN_ID))
                    .thenReturn(List.of(inst1, inst2));
            when(installmentRepository.save(inst2)).thenReturn(inst2);
            when(loanMapper.toInstallmentResponse(inst2)).thenReturn(resp);

            LoanInstallmentResponse result = loanInstallmentService.payInstallment(LOAN_ID, 2L, USER_ID);

            assertThat(inst2.isPaid()).isTrue();
            assertThat(loan.getPaidInstallments()).isEqualTo(2);
        }
    }
}
