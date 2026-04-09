package com.financialapp.finances.service;

import com.financialapp.finances.exception.ResourceNotFoundException;
import com.financialapp.finances.mapper.CardExpenseMapper;
import com.financialapp.finances.model.dto.request.CardExpenseRequest;
import com.financialapp.finances.model.dto.request.CardExpenseUpdateRequest;
import com.financialapp.finances.model.dto.response.CardExpenseResponse;
import com.financialapp.finances.model.entity.CardExpense;
import com.financialapp.finances.model.entity.CardExpenseInstallment;
import com.financialapp.finances.repository.CardExpenseInstallmentRepository;
import com.financialapp.finances.repository.CardExpenseRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardExpenseService")
class CardExpenseServiceTest {

    @Mock private CardExpenseRepository cardExpenseRepository;
    @Mock private CardExpenseInstallmentRepository installmentRepository;
    @Mock private CardExpenseMapper cardExpenseMapper;

    @InjectMocks private CardExpenseService cardExpenseService;

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;

    private CardExpense buildExpense(Long id, Long userId) {
        return CardExpense.builder()
                .id(id)
                .userId(userId)
                .cardId(10L)
                .description("Heladera")
                .totalAmount(new BigDecimal("60000"))
                .currency("ARS")
                .totalInstallments(6)
                .remainingInstallments(6)
                .installmentAmount(new BigDecimal("10000"))
                .nextDueDate(LocalDate.of(2025, 2, 5))
                .active(true)
                .build();
    }

    private CardExpenseResponse buildResponse(CardExpense e) {
        return CardExpenseResponse.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .description(e.getDescription())
                .totalInstallments(e.getTotalInstallments())
                .remainingInstallments(e.getRemainingInstallments())
                .active(e.isActive())
                .build();
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("creates card expense and generates installments with monthly due dates")
        void createsExpenseAndInstallments() {
            CardExpenseRequest request = new CardExpenseRequest();
            request.setCardId(10L);
            request.setDescription("Heladera");
            request.setTotalAmount(new BigDecimal("60000"));
            request.setCurrency("ARS");
            request.setTotalInstallments(3);
            request.setInstallmentAmount(new BigDecimal("20000"));
            request.setNextDueDate(LocalDate.of(2025, 2, 5));

            CardExpense saved = buildExpense(1L, USER_ID);
            saved.setTotalInstallments(3);
            saved.setRemainingInstallments(3);

            when(cardExpenseRepository.save(any(CardExpense.class))).thenReturn(saved);
            when(cardExpenseMapper.toResponse(saved)).thenReturn(buildResponse(saved));

            CardExpenseResponse result = cardExpenseService.create(USER_ID, request);

            assertThat(result.getId()).isEqualTo(1L);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<CardExpenseInstallment>> captor = ArgumentCaptor.forClass(List.class);
            verify(installmentRepository).saveAll(captor.capture());

            List<CardExpenseInstallment> installments = captor.getValue();
            assertThat(installments).hasSize(3);
            assertThat(installments.get(0).getDueDate()).isEqualTo(LocalDate.of(2025, 2, 5));
            assertThat(installments.get(1).getDueDate()).isEqualTo(LocalDate.of(2025, 3, 5));
            assertThat(installments.get(2).getDueDate()).isEqualTo(LocalDate.of(2025, 4, 5));
            assertThat(installments.get(0).isPaid()).isFalse();
        }

        @Test
        @DisplayName("sets remainingInstallments equal to totalInstallments on creation")
        void remainingEqualsTotalOnCreate() {
            CardExpenseRequest request = new CardExpenseRequest();
            request.setCardId(10L);
            request.setDescription("TV");
            request.setTotalAmount(new BigDecimal("120000"));
            request.setCurrency("ARS");
            request.setTotalInstallments(12);
            request.setInstallmentAmount(new BigDecimal("10000"));
            request.setNextDueDate(LocalDate.now());

            ArgumentCaptor<CardExpense> captor = ArgumentCaptor.forClass(CardExpense.class);
            CardExpense saved = buildExpense(1L, USER_ID);
            when(cardExpenseRepository.save(captor.capture())).thenReturn(saved);
            when(cardExpenseMapper.toResponse(any())).thenReturn(buildResponse(saved));

            cardExpenseService.create(USER_ID, request);

            CardExpense captured = captor.getValue();
            assertThat(captured.getRemainingInstallments()).isEqualTo(captured.getTotalInstallments());
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("returns expense when owned by user")
        void returnsExpense() {
            CardExpense expense = buildExpense(1L, USER_ID);
            when(cardExpenseRepository.findById(1L)).thenReturn(Optional.of(expense));
            when(cardExpenseMapper.toResponse(expense)).thenReturn(buildResponse(expense));

            CardExpenseResponse result = cardExpenseService.getById(1L, USER_ID);

            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when not found")
        void throwsWhenNotFound() {
            when(cardExpenseRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cardExpenseService.getById(99L, USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when owned by other user")
        void throwsWhenWrongUser() {
            CardExpense expense = buildExpense(1L, OTHER_USER_ID);
            when(cardExpenseRepository.findById(1L)).thenReturn(Optional.of(expense));

            assertThatThrownBy(() -> cardExpenseService.getById(1L, USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("updates cardId and description")
        void updatesFields() {
            CardExpense expense = buildExpense(1L, USER_ID);
            CardExpenseUpdateRequest request = new CardExpenseUpdateRequest();
            request.setCardId(20L);
            request.setDescription("Nueva descripción");

            when(cardExpenseRepository.findById(1L)).thenReturn(Optional.of(expense));
            when(cardExpenseRepository.save(expense)).thenReturn(expense);
            when(cardExpenseMapper.toResponse(expense)).thenReturn(buildResponse(expense));

            cardExpenseService.update(1L, USER_ID, request);

            assertThat(expense.getCardId()).isEqualTo(20L);
            assertThat(expense.getDescription()).isEqualTo("Nueva descripción");
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("deletes expense when owned by user")
        void deletesExpense() {
            CardExpense expense = buildExpense(1L, USER_ID);
            when(cardExpenseRepository.findById(1L)).thenReturn(Optional.of(expense));

            cardExpenseService.delete(1L, USER_ID);

            verify(cardExpenseRepository).delete(expense);
        }

        @Test
        @DisplayName("throws when not owned by user")
        void throwsWhenNotOwned() {
            CardExpense expense = buildExpense(1L, OTHER_USER_ID);
            when(cardExpenseRepository.findById(1L)).thenReturn(Optional.of(expense));

            assertThatThrownBy(() -> cardExpenseService.delete(1L, USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(cardExpenseRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("markClosedIfFullyPaid")
    class MarkClosedIfFullyPaid {

        @Test
        @DisplayName("marks inactive when remainingInstallments reaches 0")
        void marksInactiveWhenZeroRemaining() {
            CardExpense expense = buildExpense(1L, USER_ID);
            expense.setRemainingInstallments(0);

            cardExpenseService.markClosedIfFullyPaid(expense);

            assertThat(expense.isActive()).isFalse();
            verify(cardExpenseRepository).save(expense);
        }

        @Test
        @DisplayName("stays active when remaining > 0")
        void staysActiveWhenRemaining() {
            CardExpense expense = buildExpense(1L, USER_ID);
            expense.setRemainingInstallments(2);

            cardExpenseService.markClosedIfFullyPaid(expense);

            assertThat(expense.isActive()).isTrue();
            verify(cardExpenseRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateNextPaymentDate")
    class UpdateNextPaymentDate {

        @Test
        @DisplayName("sets nextDueDate to earliest unpaid installment")
        void setsNextDueDateFromFirstUnpaid() {
            CardExpense expense = buildExpense(1L, USER_ID);
            CardExpenseInstallment next = CardExpenseInstallment.builder()
                    .installmentNumber(2)
                    .dueDate(LocalDate.of(2025, 3, 5))
                    .paid(false)
                    .build();

            when(installmentRepository.findUnpaidByCardExpenseId(1L)).thenReturn(List.of(next));

            cardExpenseService.updateNextPaymentDate(expense);

            assertThat(expense.getNextDueDate()).isEqualTo(LocalDate.of(2025, 3, 5));
            verify(cardExpenseRepository).save(expense);
        }

        @Test
        @DisplayName("saves expense even when no unpaid installments remain")
        void savesWhenNoUnpaid() {
            CardExpense expense = buildExpense(1L, USER_ID);
            when(installmentRepository.findUnpaidByCardExpenseId(1L)).thenReturn(List.of());

            cardExpenseService.updateNextPaymentDate(expense);

            verify(cardExpenseRepository).save(expense);
        }
    }
}
