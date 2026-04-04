package com.financialapp.finances.kafka.producer;

import com.financialapp.finances.kafka.event.InstallmentReminderEvent;
import com.financialapp.finances.kafka.event.LoanReminderEvent;
import com.financialapp.finances.kafka.event.PaymentDueEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FinancesEventProducer {

    private static final String TOPIC_PAYMENT_DUE = "payment.due";
    private static final String TOPIC_LOAN_REMINDER = "loan.reminder";
    private static final String TOPIC_INSTALLMENT_REMINDER = "installment.reminder";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentDue(PaymentDueEvent event) {
        log.info("Publishing payment.due event for userId={}, cardExpenseId={}",
                event.getUserId(), event.getPayload().getCardExpenseId());
        kafkaTemplate.send(TOPIC_PAYMENT_DUE, String.valueOf(event.getUserId()), event);
    }

    public void publishLoanReminder(LoanReminderEvent event) {
        log.info("Publishing loan.reminder event for userId={}, loanId={}",
                event.getUserId(), event.getPayload().getLoanId());
        kafkaTemplate.send(TOPIC_LOAN_REMINDER, String.valueOf(event.getUserId()), event);
    }

    public void publishInstallmentReminder(InstallmentReminderEvent event) {
        log.info("Publishing installment.reminder event for userId={}, loanId={}, installmentId={}",
                event.getUserId(), event.getPayload().getLoanId(), event.getPayload().getInstallmentId());
        kafkaTemplate.send(TOPIC_INSTALLMENT_REMINDER, String.valueOf(event.getUserId()), event);
    }
}
