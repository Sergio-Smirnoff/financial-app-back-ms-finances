package com.financialapp.finances.infrastructure.messaging.listener;

import com.financialapp.commons.messaging.infrastructure.messaging.consume.IdempotentEventProcessor;
import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.Money;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.exception.UnsupportedCurrencyException;
import com.financialapp.finances.domain.gateway.SupportedCurrencies;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.infrastructure.messaging.payload.PaymentRecordedData;
import com.financialapp.finances.infrastructure.persistence.repository.SystemCategoryResolver;
import io.cloudevents.CloudEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Currency;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    static final Set<String> INCOME_KEYWORDS = Set.of("deposit", "received", "ingreso");

    private final TransactionRepository transactionRepository;
    private final SystemCategoryResolver systemCategories;
    private final SupportedCurrencies supportedCurrencies;
    private final IdempotentEventProcessor processor;

    @KafkaListener(topics = "banks.payment.recorded", groupId = "${spring.application.name}-group")
    @Transactional
    public void onPaymentEvent(CloudEvent event) {
        processor.process(event, PaymentRecordedData.class, this::record);
    }

    private void record(PaymentRecordedData data) {
        boolean income = isIncome(data.description());
        Cbu account = new Cbu(data.accountCbu());
        Cbu from = income ? Cbu.EXTERNAL_INSTALLMENT_CBU : account;
        Cbu to = income ? account : Cbu.EXTERNAL_INSTALLMENT_CBU;

        Currency currency = Currency.getInstance(data.currency());
        if (!supportedCurrencies.isSupported(currency)) {
            throw new UnsupportedCurrencyException(data.currency(), supportedCurrencies.all());
        }

        Long categoryId = systemCategories
                .findUnassignedCategoryId()
                .orElseThrow(() -> new IllegalStateException("System 'Unassigned' category missing"));

        Transaction tx = Transaction.create(
                new UserId(data.userId()), from, to,
                new Money(data.amount().abs(), currency),
                new CategoryId(categoryId),
                data.description() == null ? "Automatic payment" : data.description(),
                data.date() == null ? LocalDate.now() : data.date());

        transactionRepository.save(tx);
        log.info("Recorded ledger transaction from banks.payment.recorded for user {}", data.userId());
    }

    private boolean isIncome(String description) {
        if (description == null) return false;
        String d = description.toLowerCase();
        return INCOME_KEYWORDS.stream().anyMatch(d::contains);
    }
}
