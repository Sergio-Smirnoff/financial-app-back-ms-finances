package com.financialapp.finances.service;

import com.financialapp.finances.exception.ResourceNotFoundException;
import com.financialapp.finances.mapper.LoanMapper;
import com.financialapp.finances.model.dto.request.LoanRequest;
import com.financialapp.finances.model.dto.request.LoanUpdateRequest;
import com.financialapp.finances.model.dto.response.LoanResponse;
import com.financialapp.finances.model.entity.Loan;
import com.financialapp.finances.model.entity.LoanInstallment;
import com.financialapp.finances.repository.LoanInstallmentRepository;
import com.financialapp.finances.repository.LoanRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoanService")
class LoanServiceTest {

    @Mock private LoanRepository loanRepository;
    @Mock private LoanInstallmentRepository installmentRepository;
    @Mock private LoanMapper loanMapper;

    @InjectMocks private LoanService loanService;

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;

    private Loan buildLoan(Long id, Long userId) {
        return Loan.builder()
                .id(id)
                .userId(userId)
                .description("Préstamo personal")
                .entity("Banco Nación")
                .totalAmount(new BigDecimal("120000"))
                .currency("ARS")
                .totalInstallments(12)
                .paidInstallments(0)
                .installmentAmount(new BigDecimal("10000"))
                .nextPaymentDate(LocalDate.of(2025, 2, 1))
                .active(true)
                .build();
    }

    private LoanResponse buildResponse(Loan loan) {
        return LoanResponse.builder()
                .id(loan.getId())
                .userId(loan.getUserId())
                .description(loan.getDescription())
                .totalInstallments(loan.getTotalInstallments())
                .paidInstallments(loan.getPaidInstallments())
                .active(loan.isActive())
                .build();
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("creates loan and generates all installments with correct due dates")
        void createsLoanAndInstallments() {
            LoanRequest request = new LoanRequest();
            request.setDescription("Préstamo personal");
            request.setEntity("Banco Nación");
            request.setTotalAmount(new BigDecimal("120000"));
            request.setCurrency("ARS");
            request.setTotalInstallments(3);
            request.setInstallmentAmount(new BigDecimal("40000"));
            request.setFirstPaymentDate(LocalDate.of(2025, 2, 1));

            Loan saved = buildLoan(1L, USER_ID);
            saved.setTotalInstallments(3);
            LoanResponse resp = buildResponse(saved);

            when(loanRepository.save(any(Loan.class))).thenReturn(saved);
            when(loanMapper.toResponse(saved)).thenReturn(resp);

            LoanResponse result = loanService.create(USER_ID, request);

            assertThat(result.getId()).isEqualTo(1L);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<LoanInstallment>> captor = ArgumentCaptor.forClass(List.class);
            verify(installmentRepository).saveAll(captor.capture());

            List<LoanInstallment> installments = captor.getValue();
            assertThat(installments).hasSize(3);
            assertThat(installments.get(0).getInstallmentNumber()).isEqualTo(1);
            assertThat(installments.get(0).getDueDate()).isEqualTo(LocalDate.of(2025, 2, 1));
            assertThat(installments.get(1).getDueDate()).isEqualTo(LocalDate.of(2025, 3, 1));
            assertThat(installments.get(2).getDueDate()).isEqualTo(LocalDate.of(2025, 4, 1));
            assertThat(installments.get(0).isPaid()).isFalse();
        }

        @Test
        @DisplayName("creates loan with paidInstallments = 0 and active = true")
        void loanStartsActiveWithZeroPaid() {
            LoanRequest request = new LoanRequest();
            request.setDescription("Test");
            request.setTotalAmount(BigDecimal.TEN);
            request.setCurrency("ARS");
            request.setTotalInstallments(1);
            request.setInstallmentAmount(BigDecimal.TEN);
            request.setFirstPaymentDate(LocalDate.now());

            ArgumentCaptor<Loan> loanCaptor = ArgumentCaptor.forClass(Loan.class);
            Loan saved = buildLoan(1L, USER_ID);
            saved.setTotalInstallments(1);
            when(loanRepository.save(loanCaptor.capture())).thenReturn(saved);
            when(loanMapper.toResponse(any())).thenReturn(buildResponse(saved));

            loanService.create(USER_ID, request);

            Loan captured = loanCaptor.getValue();
            assertThat(captured.getPaidInstallments()).isEqualTo(0);
            assertThat(captured.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("returns loan when owned by user")
        void returnsLoan() {
            Loan loan = buildLoan(1L, USER_ID);
            LoanResponse resp = buildResponse(loan);
            when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
            when(loanMapper.toResponse(loan)).thenReturn(resp);

            LoanResponse result = loanService.getById(1L, USER_ID);

            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when not found")
        void throwsWhenNotFound() {
            when(loanRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> loanService.getById(99L, USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when owned by other user")
        void throwsWhenWrongUser() {
            Loan loan = buildLoan(1L, OTHER_USER_ID);
            when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

            assertThatThrownBy(() -> loanService.getById(1L, USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("updates description and entity only")
        void updatesDescriptionAndEntity() {
            Loan loan = buildLoan(1L, USER_ID);
            LoanUpdateRequest request = new LoanUpdateRequest();
            request.setDescription("Nuevo descripción");
            request.setEntity("Banco Provincia");

            LoanResponse resp = LoanResponse.builder().id(1L).description("Nuevo descripción").build();
            when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
            when(loanRepository.save(loan)).thenReturn(loan);
            when(loanMapper.toResponse(loan)).thenReturn(resp);

            LoanResponse result = loanService.update(1L, USER_ID, request);

            assertThat(loan.getDescription()).isEqualTo("Nuevo descripción");
            assertThat(loan.getEntity()).isEqualTo("Banco Provincia");
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("deletes loan when owned by user")
        void deletesLoan() {
            Loan loan = buildLoan(1L, USER_ID);
            when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

            loanService.delete(1L, USER_ID);

            verify(loanRepository).delete(loan);
        }

        @Test
        @DisplayName("throws when not owned by user")
        void throwsWhenNotOwned() {
            Loan loan = buildLoan(1L, OTHER_USER_ID);
            when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

            assertThatThrownBy(() -> loanService.delete(1L, USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(loanRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("markLoanClosedIfFullyPaid")
    class MarkLoanClosedIfFullyPaid {

        @Test
        @DisplayName("marks loan inactive when all installments paid")
        void marksInactiveWhenFullyPaid() {
            Loan loan = buildLoan(1L, USER_ID);
            loan.setTotalInstallments(3);
            loan.setPaidInstallments(3);

            loanService.markLoanClosedIfFullyPaid(loan);

            assertThat(loan.isActive()).isFalse();
            assertThat(loan.getNextPaymentDate()).isNull();
            verify(loanRepository).save(loan);
        }

        @Test
        @DisplayName("does not change state when not fully paid")
        void staysActiveWhenNotFullyPaid() {
            Loan loan = buildLoan(1L, USER_ID);
            loan.setTotalInstallments(3);
            loan.setPaidInstallments(2);

            loanService.markLoanClosedIfFullyPaid(loan);

            assertThat(loan.isActive()).isTrue();
            verify(loanRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateNextPaymentDate")
    class UpdateNextPaymentDate {

        @Test
        @DisplayName("sets next payment date to earliest unpaid installment")
        void setsNextDateFromFirstUnpaid() {
            Loan loan = buildLoan(1L, USER_ID);
            LoanInstallment next = LoanInstallment.builder()
                    .installmentNumber(2)
                    .dueDate(LocalDate.of(2025, 3, 1))
                    .paid(false)
                    .build();

            when(installmentRepository.findUnpaidByLoanId(1L)).thenReturn(List.of(next));

            loanService.updateNextPaymentDate(loan);

            assertThat(loan.getNextPaymentDate()).isEqualTo(LocalDate.of(2025, 3, 1));
            verify(loanRepository).save(loan);
        }

        @Test
        @DisplayName("sets next payment date to null when no unpaid installments remain")
        void setsNullWhenAllPaid() {
            Loan loan = buildLoan(1L, USER_ID);
            when(installmentRepository.findUnpaidByLoanId(1L)).thenReturn(List.of());

            loanService.updateNextPaymentDate(loan);

            assertThat(loan.getNextPaymentDate()).isNull();
            verify(loanRepository).save(loan);
        }
    }
}
