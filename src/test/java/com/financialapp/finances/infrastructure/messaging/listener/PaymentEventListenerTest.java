package com.financialapp.finances.infrastructure.messaging.listener;

import com.financialapp.commons.messaging.infrastructure.messaging.consume.IdempotentEventProcessor;
import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.exception.UnsupportedCurrencyException;
import com.financialapp.finances.domain.gateway.SupportedCurrencies;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.infrastructure.messaging.payload.PaymentRecordedData;
import com.financialapp.finances.infrastructure.persistence.repository.SystemCategoryResolver;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentEventListenerTest {

    private final TransactionRepository repo = mock(TransactionRepository.class);
    private final SystemCategoryResolver categories = mock(SystemCategoryResolver.class);
    private final SupportedCurrencies supportedCurrencies = mock(SupportedCurrencies.class);
    private final IdempotentEventProcessor processor = mock(IdempotentEventProcessor.class);
    private final PaymentEventListener listener =
            new PaymentEventListener(repo, categories, supportedCurrencies, processor);

    private PaymentRecordedData loanPayment() {
        return new PaymentRecordedData(42L, "0001112223334445556667", new BigDecimal("1250.00"),
                "ARS", "Loan Payment: Car (Installment 3)", LocalDate.of(2026, 6, 1));
    }

    private void callRecord(PaymentRecordedData data) throws Exception {
        Method m = PaymentEventListener.class.getDeclaredMethod("record", PaymentRecordedData.class);
        m.setAccessible(true);
        m.invoke(listener, data);
    }

    @Test
    void recordsExpenseWithSentinelCounterpartyAndDoesNotEcho() throws Exception {
        when(supportedCurrencies.isSupported(any())).thenReturn(true);
        when(categories.findUnassignedCategoryId()).thenReturn(Optional.of(900L));
        when(repo.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        callRecord(loanPayment());

        ArgumentCaptor<Transaction> cap = ArgumentCaptor.forClass(Transaction.class);
        verify(repo).save(cap.capture());
        Transaction t = cap.getValue();
        assertThat(t.fromCbu()).isEqualTo(new Cbu("0001112223334445556667"));
        assertThat(t.toCbu()).isEqualTo(Cbu.EXTERNAL_INSTALLMENT_CBU);
    }

    @Test
    void incomeWhenDescriptionIndicatesADeposit() throws Exception {
        when(supportedCurrencies.isSupported(any())).thenReturn(true);
        when(categories.findUnassignedCategoryId()).thenReturn(Optional.of(901L));
        when(repo.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        callRecord(new PaymentRecordedData(42L, "0001112223334445556667",
                new BigDecimal("500000.00"), "ARS", "Loan Deposit: Car", LocalDate.of(2026, 6, 1)));

        ArgumentCaptor<Transaction> cap = ArgumentCaptor.forClass(Transaction.class);
        verify(repo).save(cap.capture());
        assertThat(cap.getValue().fromCbu()).isEqualTo(Cbu.EXTERNAL_INSTALLMENT_CBU);
        assertThat(cap.getValue().toCbu()).isEqualTo(new Cbu("0001112223334445556667"));
    }

    @Test
    void rejectsUnsupportedCurrencyAndRecordsNothing() {
        when(supportedCurrencies.isSupported(Currency.getInstance("JPY"))).thenReturn(false);
        when(supportedCurrencies.all()).thenReturn(Set.of(Currency.getInstance("ARS")));

        assertThatThrownBy(() ->
                callRecord(new PaymentRecordedData(42L, "0001112223334445556667",
                        new BigDecimal("100.00"), "JPY", "Loan Payment", LocalDate.of(2026, 6, 1))))
                .hasCauseInstanceOf(UnsupportedCurrencyException.class);

        verify(repo, never()).save(any());
    }

    @Test
    void incomeKeywordsAreTheExpectedSet() {
        assertThat(PaymentEventListener.INCOME_KEYWORDS)
                .containsExactlyInAnyOrder("deposit", "received", "ingreso");
    }
}
