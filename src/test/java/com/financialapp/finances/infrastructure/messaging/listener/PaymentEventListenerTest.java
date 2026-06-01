package com.financialapp.finances.infrastructure.messaging.listener;

import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.exception.UnsupportedCurrencyException;
import com.financialapp.finances.domain.gateway.SupportedCurrencies;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.infrastructure.messaging.payload.PaymentEvent;
import com.financialapp.finances.infrastructure.persistence.entity.ProcessedInboundEventJpaEntity;
import com.financialapp.finances.infrastructure.persistence.jpa.ProcessedInboundEventJpaRepository;
import com.financialapp.finances.infrastructure.persistence.repository.SystemCategoryResolver;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PaymentEventListenerTest {

    private final TransactionRepository repo = mock(TransactionRepository.class);
    private final ProcessedInboundEventJpaRepository processed = mock(ProcessedInboundEventJpaRepository.class);
    private final SystemCategoryResolver categories = mock(SystemCategoryResolver.class);
    private final SupportedCurrencies supportedCurrencies = mock(SupportedCurrencies.class);
    private final PaymentEventListener listener =
            new PaymentEventListener(repo, processed, categories, supportedCurrencies);

    private PaymentEvent loanPayment() {
        return new PaymentEvent(42L, "0001112223334445556667", new BigDecimal("1250.00"),
                "ARS", "Loan Payment: Car (Installment 3)", LocalDate.of(2026, 6, 1));
    }

    @Test
    void recordsExpenseWithSentinelCounterpartyAndDoesNotEcho() {
        when(processed.existsById(anyString())).thenReturn(false);
        when(supportedCurrencies.isSupported(any())).thenReturn(true);
        when(categories.findUnassignedCategoryId()).thenReturn(Optional.of(900L));
        when(repo.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        listener.onPaymentEvent(loanPayment());

        ArgumentCaptor<Transaction> cap = ArgumentCaptor.forClass(Transaction.class);
        verify(repo).save(cap.capture());
        Transaction t = cap.getValue();
        assertThat(t.fromCbu()).isEqualTo(new Cbu("0001112223334445556667"));
        assertThat(t.toCbu()).isEqualTo(Cbu.EXTERNAL_INSTALLMENT_CBU);
        // No balance echo: the listener has no DomainEventPublisher dependency at all (structural).
        verify(processed).save(any(ProcessedInboundEventJpaEntity.class));
    }

    @Test
    void incomeWhenDescriptionIndicatesADeposit() {
        when(processed.existsById(anyString())).thenReturn(false);
        when(supportedCurrencies.isSupported(any())).thenReturn(true);
        when(categories.findUnassignedCategoryId()).thenReturn(Optional.of(901L));
        when(repo.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        listener.onPaymentEvent(new PaymentEvent(42L, "0001112223334445556667",
                new BigDecimal("500000.00"), "ARS", "Loan Deposit: Car", LocalDate.of(2026, 6, 1)));

        ArgumentCaptor<Transaction> cap = ArgumentCaptor.forClass(Transaction.class);
        verify(repo).save(cap.capture());
        assertThat(cap.getValue().fromCbu()).isEqualTo(Cbu.EXTERNAL_INSTALLMENT_CBU);
        assertThat(cap.getValue().toCbu()).isEqualTo(new Cbu("0001112223334445556667"));
    }

    @Test
    void rejectsUnsupportedCurrencyAndRecordsNothing() {
        when(processed.existsById(anyString())).thenReturn(false);
        when(supportedCurrencies.isSupported(Currency.getInstance("JPY"))).thenReturn(false);
        when(supportedCurrencies.all()).thenReturn(Set.of(Currency.getInstance("ARS")));

        assertThatThrownBy(() ->
                listener.onPaymentEvent(new PaymentEvent(42L, "0001112223334445556667",
                        new BigDecimal("100.00"), "JPY", "Loan Payment", LocalDate.of(2026, 6, 1))))
                .isInstanceOf(UnsupportedCurrencyException.class);

        verify(repo, never()).save(any());
    }

    @Test
    void redeliveryIsDeduped() {
        when(processed.existsById(anyString())).thenReturn(true);

        listener.onPaymentEvent(loanPayment());

        verify(repo, never()).save(any());
    }
}
