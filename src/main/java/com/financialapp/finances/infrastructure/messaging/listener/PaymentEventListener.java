package com.financialapp.finances.infrastructure.messaging.listener;

import com.financialapp.finances.domain.common.model.*;
import com.financialapp.finances.domain.exception.UnsupportedCurrencyException;
import com.financialapp.finances.domain.gateway.SupportedCurrencies;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.infrastructure.messaging.payload.PaymentEvent;
import com.financialapp.finances.infrastructure.persistence.entity.ProcessedInboundEventJpaEntity;
import com.financialapp.finances.infrastructure.persistence.jpa.ProcessedInboundEventJpaRepository;
import com.financialapp.finances.infrastructure.persistence.repository.SystemCategoryResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Currency;
import java.util.HexFormat;
import java.util.Set;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * ACL in: ms-banks {@code payment-events} → a ledger Transaction. ms-banks already moved its own
 * balance, so this path records the row WITHOUT publishing a balance event (no echo). The external
 * side is {@link Cbu#EXTERNAL_INSTALLMENT_CBU}; direction is inferred from the description.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    /** Description keywords that mark an inbound payment as income; anything else is an expense. */
    static final Set<String> INCOME_KEYWORDS = Set.of("deposit", "received", "ingreso");

    private final TransactionRepository transactionRepository;
    private final ProcessedInboundEventJpaRepository processedEvents;
    private final SystemCategoryResolver systemCategories;
    private final SupportedCurrencies supportedCurrencies;

    @KafkaListener(topics = "payment-events", groupId = "${spring.application.name}-group")
    @Transactional
    public void onPaymentEvent(PaymentEvent event) {
        String dedupKey = dedupKey(event);
        if (processedEvents.existsById(dedupKey)) {
            log.warn("Duplicate payment-event {} — skipping", dedupKey);
            return;
        }

        boolean income = isIncome(event.description());
        Cbu account = new Cbu(event.accountCbu());
        Cbu from = income ? Cbu.EXTERNAL_INSTALLMENT_CBU : account;
        Cbu to = income ? account : Cbu.EXTERNAL_INSTALLMENT_CBU;

        Currency currency = Currency.getInstance(event.currency());
        if (!supportedCurrencies.isSupported(currency)) {
            throw new UnsupportedCurrencyException(event.currency(), supportedCurrencies.all());
        }

        Long categoryId = systemCategories
                .findUnassignedCategoryId()
                .orElseThrow(() -> new IllegalStateException("System 'Unassigned' category missing"));

        Transaction tx = Transaction.create(
                new UserId(event.userId()), from, to,
                new Money(event.amount().abs(), currency),
                new CategoryId(categoryId),
                event.description() == null ? "Automatic payment" : event.description(),
                event.date() == null ? LocalDate.now() : event.date());

        transactionRepository.save(tx);                 // NO domainEventPublisher call → no echo
        processedEvents.save(ProcessedInboundEventJpaEntity.builder().dedupKey(dedupKey).build());
        log.info("Recorded ledger transaction from payment-event {}", dedupKey);
    }

    private boolean isIncome(String description) {
        if (description == null) return false;           // default: expense
        String d = description.toLowerCase();
        return INCOME_KEYWORDS.stream().anyMatch(d::contains);
    }

    /** Deterministic key over the event fields (payment-events carries no native id). */
    private String dedupKey(PaymentEvent e) {
        String raw = e.userId() + "|" + e.accountCbu() + "|" + e.amount() + "|"
                + e.currency() + "|" + e.date() + "|" + e.description();
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
