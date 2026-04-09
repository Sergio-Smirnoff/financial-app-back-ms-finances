package com.financialapp.finances.scheduler;

import com.financialapp.finances.config.AlertProperties;
import com.financialapp.finances.kafka.event.InstallmentReminderEvent;
import com.financialapp.finances.kafka.event.LoanReminderEvent;
import com.financialapp.finances.kafka.event.PaymentDueEvent;
import com.financialapp.finances.kafka.producer.FinancesEventProducer;
import com.financialapp.finances.model.entity.CardExpense;
import com.financialapp.finances.model.entity.Loan;
import com.financialapp.finances.model.entity.LoanInstallment;
import com.financialapp.finances.repository.CardExpenseRepository;
import com.financialapp.finances.repository.LoanInstallmentRepository;
import com.financialapp.finances.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("FinancesAlertScheduler")
class FinancesAlertSchedulerTest {

    @Mock private AlertProperties alertProperties;
    @Mock private CardExpenseRepository cardExpenseRepository;
    @Mock private LoanRepository loanRepository;
    @Mock private LoanInstallmentRepository installmentRepository;
    @Mock private FinancesEventProducer eventProducer;

    @InjectMocks private FinancesAlertScheduler scheduler;

    @BeforeEach
    void setupAlertProperties() {
        when(alertProperties.getDaysBeforePayment()).thenReturn(3);
        when(alertProperties.getDaysBeforeLoan()).thenReturn(3);
        when(alertProperties.getDaysBeforeInstallment()).thenReturn(3);
    }

    private CardExpense buildCardExpense(Long id, Long userId) {
        return CardExpense.builder()
                .id(id)
                .userId(userId)
                .description("Heladera")
                .installmentAmount(new BigDecimal("10000"))
                .currency("ARS")
                .remainingInstallments(4)
                .nextDueDate(LocalDate.now().plusDays(2))
                .active(true)
                .build();
    }

    private Loan buildLoanWithNextPayment(Long id, Long userId, LocalDate nextPaymentDate) {
        return Loan.builder()
                .id(id)
                .userId(userId)
                .description("Préstamo auto")
                .installmentAmount(new BigDecimal("15000"))
                .currency("ARS")
                .totalInstallments(12)
                .paidInstallments(3)
                .nextPaymentDate(nextPaymentDate)
                .active(true)
                .build();
    }

    @Nested
    @DisplayName("checkCardExpensesDue")
    class CheckCardExpensesDue {

        @Test
        @DisplayName("publishes PaymentDueEvent for each upcoming card expense")
        void publishesEventForEachExpense() {
            CardExpense e1 = buildCardExpense(1L, 10L);
            CardExpense e2 = buildCardExpense(2L, 20L);

            when(cardExpenseRepository.findActiveWithUpcomingDueDate(any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(List.of(e1, e2));

            scheduler.checkCardExpensesDue();

            verify(eventProducer, times(2)).publishPaymentDue(any(PaymentDueEvent.class));
        }

        @Test
        @DisplayName("publishes event with correct userId and payload")
        void publishesCorrectEventPayload() {
            CardExpense expense = buildCardExpense(1L, 42L);
            when(cardExpenseRepository.findActiveWithUpcomingDueDate(any(), any()))
                    .thenReturn(List.of(expense));

            ArgumentCaptor<PaymentDueEvent> captor = ArgumentCaptor.forClass(PaymentDueEvent.class);
            scheduler.checkCardExpensesDue();

            verify(eventProducer).publishPaymentDue(captor.capture());
            PaymentDueEvent event = captor.getValue();

            assertThat(event.getUserId()).isEqualTo(42L);
            assertThat(event.getPayload().getCardExpenseId()).isEqualTo(1L);
            assertThat(event.getPayload().getDescription()).isEqualTo("Heladera");
            assertThat(event.getPayload().getCurrency()).isEqualTo("ARS");
            assertThat(event.getPayload().getRemainingInstallments()).isEqualTo(4);
        }

        @Test
        @DisplayName("does not publish events when no card expenses are due")
        void doesNotPublishWhenNoneFound() {
            when(cardExpenseRepository.findActiveWithUpcomingDueDate(any(), any()))
                    .thenReturn(List.of());

            scheduler.checkCardExpensesDue();

            verify(eventProducer, never()).publishPaymentDue(any());
        }
    }

    @Nested
    @DisplayName("checkLoansReminder")
    class CheckLoansReminder {

        @Test
        @DisplayName("publishes LoanReminderEvent for active loans with payment within window")
        void publishesEventForLoansWithinWindow() {
            Loan loan = buildLoanWithNextPayment(1L, 10L, LocalDate.now().plusDays(2));
            when(loanRepository.findAll()).thenReturn(List.of(loan));

            scheduler.checkLoansReminder();

            ArgumentCaptor<LoanReminderEvent> captor = ArgumentCaptor.forClass(LoanReminderEvent.class);
            verify(eventProducer).publishLoanReminder(captor.capture());

            LoanReminderEvent event = captor.getValue();
            assertThat(event.getUserId()).isEqualTo(10L);
            assertThat(event.getPayload().getLoanId()).isEqualTo(1L);
            assertThat(event.getPayload().getRemainingInstallments()).isEqualTo(9); // 12 - 3
        }

        @Test
        @DisplayName("does not publish for loans with nextPaymentDate outside window")
        void doesNotPublishWhenOutsideWindow() {
            Loan loan = buildLoanWithNextPayment(1L, 10L, LocalDate.now().plusDays(10));
            when(loanRepository.findAll()).thenReturn(List.of(loan));

            scheduler.checkLoansReminder();

            verify(eventProducer, never()).publishLoanReminder(any());
        }

        @Test
        @DisplayName("does not publish for inactive loans")
        void doesNotPublishForInactiveLoans() {
            Loan loan = buildLoanWithNextPayment(1L, 10L, LocalDate.now().plusDays(1));
            loan.setActive(false);
            when(loanRepository.findAll()).thenReturn(List.of(loan));

            scheduler.checkLoansReminder();

            verify(eventProducer, never()).publishLoanReminder(any());
        }

        @Test
        @DisplayName("does not publish for loans with null nextPaymentDate")
        void doesNotPublishWhenNextPaymentDateNull() {
            Loan loan = buildLoanWithNextPayment(1L, 10L, null);
            when(loanRepository.findAll()).thenReturn(List.of(loan));

            scheduler.checkLoansReminder();

            verify(eventProducer, never()).publishLoanReminder(any());
        }
    }

    @Nested
    @DisplayName("checkInstallmentsReminder")
    class CheckInstallmentsReminder {

        @Test
        @DisplayName("publishes InstallmentReminderEvent for each upcoming unpaid installment")
        void publishesEventForEachInstallment() {
            Loan loan = buildLoanWithNextPayment(1L, 10L, LocalDate.now().plusDays(2));
            LoanInstallment inst1 = LoanInstallment.builder()
                    .id(100L)
                    .loan(loan)
                    .installmentNumber(4)
                    .amount(new BigDecimal("15000"))
                    .dueDate(LocalDate.now().plusDays(2))
                    .paid(false)
                    .build();
            LoanInstallment inst2 = LoanInstallment.builder()
                    .id(101L)
                    .loan(loan)
                    .installmentNumber(5)
                    .amount(new BigDecimal("15000"))
                    .dueDate(LocalDate.now().plusDays(3))
                    .paid(false)
                    .build();

            when(installmentRepository.findUpcomingUnpaid(any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(List.of(inst1, inst2));

            scheduler.checkInstallmentsReminder();

            verify(eventProducer, times(2)).publishInstallmentReminder(any(InstallmentReminderEvent.class));
        }

        @Test
        @DisplayName("publishes event with correct installment details")
        void publishesCorrectInstallmentPayload() {
            Loan loan = buildLoanWithNextPayment(1L, 10L, LocalDate.now().plusDays(1));
            LoanInstallment inst = LoanInstallment.builder()
                    .id(100L)
                    .loan(loan)
                    .installmentNumber(4)
                    .amount(new BigDecimal("15000"))
                    .dueDate(LocalDate.now().plusDays(1))
                    .paid(false)
                    .build();

            when(installmentRepository.findUpcomingUnpaid(any(), any())).thenReturn(List.of(inst));

            ArgumentCaptor<InstallmentReminderEvent> captor =
                    ArgumentCaptor.forClass(InstallmentReminderEvent.class);
            scheduler.checkInstallmentsReminder();

            verify(eventProducer).publishInstallmentReminder(captor.capture());
            InstallmentReminderEvent event = captor.getValue();

            assertThat(event.getUserId()).isEqualTo(10L);
            assertThat(event.getPayload().getLoanId()).isEqualTo(1L);
            assertThat(event.getPayload().getInstallmentId()).isEqualTo(100L);
            assertThat(event.getPayload().getInstallmentNumber()).isEqualTo(4);
            assertThat(event.getPayload().getAmount()).isEqualByComparingTo("15000");
        }

        @Test
        @DisplayName("does not publish when no upcoming installments")
        void doesNotPublishWhenNoneFound() {
            when(installmentRepository.findUpcomingUnpaid(any(), any())).thenReturn(List.of());

            scheduler.checkInstallmentsReminder();

            verify(eventProducer, never()).publishInstallmentReminder(any());
        }
    }
}
