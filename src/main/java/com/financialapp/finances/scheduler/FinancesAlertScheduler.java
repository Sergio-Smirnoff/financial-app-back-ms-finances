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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FinancesAlertScheduler {

    private final AlertProperties alertProperties;
    private final CardExpenseRepository cardExpenseRepository;
    private final LoanRepository loanRepository;
    private final LoanInstallmentRepository installmentRepository;
    private final FinancesEventProducer eventProducer;

    /**
     * Daily job: publishes payment.due events for card expenses whose next_due_date
     * falls within the configured alert window.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void checkCardExpensesDue() {
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(alertProperties.getDaysBeforePayment());
        List<CardExpense> upcoming = cardExpenseRepository.findActiveWithUpcomingDueDate(today, limit);
        log.info("payment.due scheduler — found {} card expense(s) due within {} days", upcoming.size(),
                alertProperties.getDaysBeforePayment());
        for (CardExpense expense : upcoming) {
            PaymentDueEvent event = PaymentDueEvent.builder()
                    .userId(expense.getUserId())
                    .payload(PaymentDueEvent.Payload.builder()
                            .cardExpenseId(expense.getId())
                            .description(expense.getDescription())
                            .nextDueDate(expense.getNextDueDate())
                            .installmentAmount(expense.getInstallmentAmount())
                            .currency(expense.getCurrency())
                            .remainingInstallments(expense.getRemainingInstallments())
                            .build())
                    .build();
            eventProducer.publishPaymentDue(event);
        }
    }

    /**
     * Daily job: publishes loan.reminder events for loans whose next_payment_date
     * falls within the configured alert window.
     */
    @Scheduled(cron = "0 5 8 * * *")
    public void checkLoansReminder() {
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(alertProperties.getDaysBeforeLoan());

        List<Loan> loansWithUpcomingPayment = loanRepository.findActiveWithUpcomingPayment(today, limit);

        log.info("loan.reminder scheduler — found {} loan(s) with payment due within {} days",
                loansWithUpcomingPayment.size(), alertProperties.getDaysBeforeLoan());

        for (Loan loan : loansWithUpcomingPayment) {
            LoanReminderEvent event = LoanReminderEvent.builder()
                    .userId(loan.getUserId())
                    .payload(LoanReminderEvent.Payload.builder()
                            .loanId(loan.getId())
                            .loanDescription(loan.getDescription())
                            .nextPaymentDate(loan.getNextPaymentDate())
                            .installmentAmount(loan.getInstallmentAmount())
                            .currency(loan.getCurrency())
                            .remainingInstallments(loan.getTotalInstallments() - loan.getPaidInstallments())
                            .build())
                    .build();
            eventProducer.publishLoanReminder(event);
        }
    }

    /**
     * Daily job: publishes installment.reminder events for individual loan installments
     * whose due_date falls within the configured alert window.
     */
    @Scheduled(cron = "0 10 8 * * *")
    public void checkInstallmentsReminder() {
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(alertProperties.getDaysBeforeInstallment());
        List<LoanInstallment> upcoming = installmentRepository.findUpcomingUnpaid(today, limit);
        log.info("installment.reminder scheduler — found {} installment(s) due within {} days",
                upcoming.size(), alertProperties.getDaysBeforeInstallment());
        for (LoanInstallment installment : upcoming) {
            Loan loan = installment.getLoan();
            InstallmentReminderEvent event = InstallmentReminderEvent.builder()
                    .userId(loan.getUserId())
                    .payload(InstallmentReminderEvent.Payload.builder()
                            .loanId(loan.getId())
                            .installmentId(installment.getId())
                            .loanDescription(loan.getDescription())
                            .installmentNumber(installment.getInstallmentNumber())
                            .dueDate(installment.getDueDate())
                            .amount(installment.getAmount())
                            .currency(loan.getCurrency())
                            .build())
                    .build();
            eventProducer.publishInstallmentReminder(event);
        }
    }
}
