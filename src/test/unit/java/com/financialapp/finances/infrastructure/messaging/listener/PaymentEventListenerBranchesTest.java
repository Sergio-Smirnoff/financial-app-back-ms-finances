package com.financialapp.finances.infrastructure.messaging.listener;

import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.infrastructure.messaging.payload.PaymentEvent;
import com.financialapp.finances.infrastructure.persistence.entity.ProcessedInboundEventJpaEntity;
import com.financialapp.finances.infrastructure.persistence.jpa.ProcessedInboundEventJpaRepository;
import com.financialapp.finances.infrastructure.persistence.repository.SystemCategoryResolver;
import com.financialapp.finances.domain.gateway.SupportedCurrencies;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PaymentEventListenerBranchesTest {

    private final TransactionRepository repo = mock(TransactionRepository.class);
    private final ProcessedInboundEventJpaRepository processed = mock(ProcessedInboundEventJpaRepository.class);
    private final SystemCategoryResolver categories = mock(SystemCategoryResolver.class);
    private final SupportedCurrencies supportedCurrencies = mock(SupportedCurrencies.class);
    private final PaymentEventListener listener =
            new PaymentEventListener(repo, processed, categories, supportedCurrencies);

    @Test void throws_whenUnassignedCategoryMissing() {
        // Given the system 'Unassigned' category does not exist
        when(processed.existsById(anyString())).thenReturn(false);
        when(supportedCurrencies.isSupported(any())).thenReturn(true);
        when(categories.findUnassignedCategoryId()).thenReturn(Optional.empty());
        // When / Then the listener fails and persists nothing
        assertThatThrownBy(() -> listener.onPaymentEvent(new PaymentEvent(42L, "0001112223334445556667",
                new BigDecimal("100.00"), "ARS", "Loan Payment", LocalDate.of(2026, 6, 1))))
                .isInstanceOf(IllegalStateException.class);
        verify(repo, never()).save(any());
    }

    @Test void defaultsDescriptionAndDate_whenNull() {
        // Given a payment-event with null description and null date
        when(processed.existsById(anyString())).thenReturn(false);
        when(supportedCurrencies.isSupported(any())).thenReturn(true);
        when(categories.findUnassignedCategoryId()).thenReturn(Optional.of(900L));
        when(repo.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        // When the event is processed
        listener.onPaymentEvent(new PaymentEvent(42L, "0001112223334445556667",
                new BigDecimal("100.00"), "ARS", null, null));

        // Then the description defaults and the date falls back to today (expense, since description is null)
        ArgumentCaptor<Transaction> cap = ArgumentCaptor.forClass(Transaction.class);
        verify(repo).save(cap.capture());
        Transaction tx = cap.getValue();
        assertThat(tx.description()).isEqualTo("Automatic payment");
        assertThat(tx.date()).isEqualTo(LocalDate.now());
        assertThat(tx.fromCbu().cbuNumber()).isEqualTo("0001112223334445556667"); // expense direction
    }
}
