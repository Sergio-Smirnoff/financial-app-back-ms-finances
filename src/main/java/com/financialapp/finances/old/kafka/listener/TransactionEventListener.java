package com.financialapp.finances.kafka.listener;

import com.financialapp.finances.kafka.event.PaymentEvent;
import com.financialapp.finances.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventListener {

    private final TransactionService transactionService;

    @KafkaListener(topics = "payment-events", groupId = "${spring.application.name}-group")
    public void listenPaymentEvents(PaymentEvent event) {
        log.info("Received payment event from Kafka: {}" , event);
        try {
            transactionService.recordPayment(event);
        } catch (Exception e) {
            log.error("Error recording payment transaction: {}", e.getMessage(), e);
        }
    }
}
