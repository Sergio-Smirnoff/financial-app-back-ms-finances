package com.financialapp.finances.kafka.producer;

import com.financialapp.finances.kafka.event.InstallmentReminderEvent;
import com.financialapp.finances.kafka.event.LoanReminderEvent;
import com.financialapp.finances.kafka.event.PaymentDueEvent;
import com.financialapp.finances.kafka.event.TransactionCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FinancesEventProducer {

    private static final String TOPIC_PAYMENT_DUE = "payment.due";
    private static final String TOPIC_LOAN_REMINDER = "loan.reminder";
    private static final String TOPIC_INSTALLMENT_REMINDER = "installment.reminder";
    private static final String TOPIC_TRANSACTION_CREATED = "transaction.created";

    private final ApplicationEventPublisher eventPublisher;

    public void publishTransactionCreated(TransactionCreatedEvent event) {
        log.info("Queuing transactional transaction.created event for transactionId={}, userId={}",
                event.transactionId(), event.userId());
        eventPublisher.publishEvent(new TransactionalKafkaEvent(TOPIC_TRANSACTION_CREATED, String.valueOf(event.userId()), event));
    }

    public void publishPaymentDue(PaymentDueEvent event) {
        log.info("Queuing transactional payment.due event for userId={}, cardExpenseId={}",
                event.getUserId(), event.getPayload().getCardExpenseId());
        eventPublisher.publishEvent(new TransactionalKafkaEvent(TOPIC_PAYMENT_DUE, String.valueOf(event.getUserId()), event));
    }

    public void publishLoanReminder(LoanReminderEvent event) {
        log.info("Queuing transactional loan.reminder event for userId={}, loanId={}",
                event.getUserId(), event.getPayload().getLoanId());
        eventPublisher.publishEvent(new TransactionalKafkaEvent(TOPIC_LOAN_REMINDER, String.valueOf(event.getUserId()), event));
    }

    public void publishInstallmentReminder(InstallmentReminderEvent event) {
        log.info("Queuing transactional installment.reminder event for userId={}, loanId={}, installmentId={}",
                event.getUserId(), event.getPayload().getLoanId(), event.getPayload().getInstallmentId());
        eventPublisher.publishEvent(new TransactionalKafkaEvent(TOPIC_INSTALLMENT_REMINDER, String.valueOf(event.getUserId()), event));
    }
}
