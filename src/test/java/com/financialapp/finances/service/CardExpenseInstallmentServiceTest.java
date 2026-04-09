package com.financialapp.finances.service;

import com.financialapp.finances.exception.BusinessException;
import com.financialapp.finances.exception.ResourceNotFoundException;
import com.financialapp.finances.mapper.CardExpenseMapper;
import com.financialapp.finances.model.dto.response.CardExpenseInstallmentResponse;
import com.financialapp.finances.model.entity.CardExpense;
import com.financialapp.finances.model.entity.CardExpenseInstallment;
import com.financialapp.finances.repository.CardExpenseInstallmentRepository;
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
@DisplayName("CardExpenseInstallmentService")
class CardExpenseInstallmentServiceTest {

    @Mock private CardExpenseInstallmentRepository installmentRepository;
    @Mock private CardExpenseService cardExpenseService;
    @Mock private CardExpenseMapper cardExpenseMapper;

    @InjectMocks private CardExpenseInstallmentService cardExpenseInstallmentService;

    private static final Long USER_ID = 1L;
    private static final Long EXPENSE_ID = 10L;

    private CardExpense buildActiveExpense() {
        return CardExpense.builder()
                .id(EXPENSE_ID)
                .userId(USER_ID)
                .description("Heladera")
                .totalAmount(new BigDecimal("30000"))
                .currency("ARS")
                .totalInstallments(3)
                .remainingInstallments(3)
                .installmentAmount(new BigDecimal("10000"))
                .nextDueDate(LocalDate.of(2025, 2, 5))
                .active(true)
                .build();
    }

    private CardExpenseInstallment buildInstallment(Long id, int number, boolean paid, CardExpense expense) {
        return CardExpenseInstallment.builder()
                .id(id)
                .cardExpense(expense)
                .installmentNumber(number)
                .amount(new BigDecimal("10000"))
                .dueDate(LocalDate.of(2025, number, 5))
                .paid(paid)
                .build();
    }

    @Nested
    @DisplayName("payInstallment")
    class PayInstallment {

        @Test
        @DisplayName("pays first installment and decrements remainingInstallments")
        void paysFirstInstallment() {
            CardExpense expense = buildActiveExpense();
            CardExpenseInstallment inst1 = buildInstallment(1L, 1, false, expense);
            CardExpenseInstallment inst2 = buildInstallment(2L, 2, false, expense);

            CardExpenseInstallmentResponse resp = CardExpenseInstallmentResponse.builder()
                    .id(1L).installmentNumber(1).paid(true).build();

            when(cardExpenseService.findOwnedExpense(EXPENSE_ID, USER_ID)).thenReturn(expense);
            when(installmentRepository.findById(1L)).thenReturn(Optional.of(inst1));
            when(installmentRepository.findByCardExpenseIdOrderByInstallmentNumberAsc(EXPENSE_ID))
                    .thenReturn(List.of(inst1, inst2));
            when(installmentRepository.save(inst1)).thenReturn(inst1);
            when(cardExpenseMapper.toInstallmentResponse(inst1)).thenReturn(resp);

            CardExpenseInstallmentResponse result =
                    cardExpenseInstallmentService.payInstallment(EXPENSE_ID, 1L, USER_ID);

            assertThat(result.isPaid()).isTrue();
            assertThat(inst1.isPaid()).isTrue();
            assertThat(inst1.getPaidDate()).isNotNull();
            assertThat(expense.getRemainingInstallments()).isEqualTo(2);
            verify(cardExpenseService).updateNextPaymentDate(expense);
            verify(cardExpenseService).markClosedIfFullyPaid(expense);
        }

        @Test
        @DisplayName("throws BusinessException when previous installment is unpaid")
        void throwsWhenPreviousUnpaid() {
            CardExpense expense = buildActiveExpense();
            CardExpenseInstallment inst1 = buildInstallment(1L, 1, false, expense);
            CardExpenseInstallment inst2 = buildInstallment(2L, 2, false, expense);

            when(cardExpenseService.findOwnedExpense(EXPENSE_ID, USER_ID)).thenReturn(expense);
            when(installmentRepository.findById(2L)).thenReturn(Optional.of(inst2));
            when(installmentRepository.findByCardExpenseIdOrderByInstallmentNumberAsc(EXPENSE_ID))
                    .thenReturn(List.of(inst1, inst2));

            assertThatThrownBy(() -> cardExpenseInstallmentService.payInstallment(EXPENSE_ID, 2L, USER_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("installment #1 is still unpaid");

            verify(installmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws BusinessException when installment already paid")
        void throwsWhenAlreadyPaid() {
            CardExpense expense = buildActiveExpense();
            CardExpenseInstallment paid = buildInstallment(1L, 1, true, expense);

            when(cardExpenseService.findOwnedExpense(EXPENSE_ID, USER_ID)).thenReturn(expense);
            when(installmentRepository.findById(1L)).thenReturn(Optional.of(paid));

            assertThatThrownBy(() -> cardExpenseInstallmentService.payInstallment(EXPENSE_ID, 1L, USER_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already been paid");
        }

        @Test
        @DisplayName("throws BusinessException when card expense is inactive")
        void throwsWhenExpenseInactive() {
            CardExpense expense = buildActiveExpense();
            expense.setActive(false);

            when(cardExpenseService.findOwnedExpense(EXPENSE_ID, USER_ID)).thenReturn(expense);

            assertThatThrownBy(() -> cardExpenseInstallmentService.payInstallment(EXPENSE_ID, 1L, USER_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("inactive card expense");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when installment belongs to different card expense")
        void throwsWhenInstallmentMismatch() {
            CardExpense expense = buildActiveExpense();
            CardExpense otherExpense = buildActiveExpense();
            otherExpense.setId(99L);
            CardExpenseInstallment inst = buildInstallment(1L, 1, false, otherExpense);

            when(cardExpenseService.findOwnedExpense(EXPENSE_ID, USER_ID)).thenReturn(expense);
            when(installmentRepository.findById(1L)).thenReturn(Optional.of(inst));

            assertThatThrownBy(() -> cardExpenseInstallmentService.payInstallment(EXPENSE_ID, 1L, USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("marks expense as inactive after paying last installment")
        void marksExpenseClosedWhenLastInstallmentPaid() {
            CardExpense expense = buildActiveExpense();
            expense.setRemainingInstallments(1);
            CardExpenseInstallment inst = buildInstallment(1L, 1, false, expense);

            CardExpenseInstallmentResponse resp = CardExpenseInstallmentResponse.builder()
                    .id(1L).paid(true).build();

            when(cardExpenseService.findOwnedExpense(EXPENSE_ID, USER_ID)).thenReturn(expense);
            when(installmentRepository.findById(1L)).thenReturn(Optional.of(inst));
            when(installmentRepository.findByCardExpenseIdOrderByInstallmentNumberAsc(EXPENSE_ID))
                    .thenReturn(List.of(inst));
            when(installmentRepository.save(inst)).thenReturn(inst);
            when(cardExpenseMapper.toInstallmentResponse(inst)).thenReturn(resp);

            cardExpenseInstallmentService.payInstallment(EXPENSE_ID, 1L, USER_ID);

            assertThat(expense.getRemainingInstallments()).isEqualTo(0);
            verify(cardExpenseService).markClosedIfFullyPaid(expense);
        }
    }
}
