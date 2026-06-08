package com.financialapp.finances.infrastructure.messaging.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.financialapp.commons.messaging.domain.gateway.ProcessedEventGateway;
import com.financialapp.commons.messaging.domain.model.EventId;
import com.financialapp.commons.messaging.infrastructure.messaging.consume.IdempotentEventProcessor;
import com.financialapp.commons.messaging.infrastructure.messaging.serde.CloudEventSerde;
import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.exception.UnsupportedCurrencyException;
import com.financialapp.finances.domain.gateway.SupportedCurrencies;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.infrastructure.messaging.payload.PaymentRecordedData;
import com.financialapp.finances.infrastructure.persistence.repository.SystemCategoryResolver;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
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
    private final ProcessedEventGateway processedEventGateway = mock(ProcessedEventGateway.class);

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final CloudEventSerde serde = new CloudEventSerde(objectMapper);
    private final IdempotentEventProcessor processor =
            new IdempotentEventProcessor(processedEventGateway, serde);

    private PaymentEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new PaymentEventListener(repo, categories, supportedCurrencies, processor);
    }

    private CloudEvent cloudEvent(String ceId, PaymentRecordedData data) throws Exception {
        byte[] bytes = objectMapper.writeValueAsBytes(data);
        return CloudEventBuilder.v1()
                .withId(ceId)
                .withSource(URI.create("ms-banks"))
                .withType("banks.payment.recorded")
                .withTime(OffsetDateTime.now())
                .withData("application/json", bytes)
                .build();
    }

    private PaymentRecordedData loanPayment() {
        return new PaymentRecordedData(42L, "0001112223334445556667", new BigDecimal("1250.00"),
                "ARS", "Loan Payment: Car (Installment 3)", LocalDate.of(2026, 6, 1));
    }

    @Test
    void recordsExpenseWithSentinelCounterparty() throws Exception {
        when(processedEventGateway.isProcessed(any())).thenReturn(false);
        when(supportedCurrencies.isSupported(any())).thenReturn(true);
        when(categories.findUnassignedCategoryId()).thenReturn(Optional.of(900L));
        when(repo.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        listener.onPaymentEvent(cloudEvent("evt-expense-1", loanPayment()));

        ArgumentCaptor<Transaction> cap = ArgumentCaptor.forClass(Transaction.class);
        verify(repo).save(cap.capture());
        Transaction t = cap.getValue();
        assertThat(t.fromCbu()).isEqualTo(new Cbu("0001112223334445556667"));
        assertThat(t.toCbu()).isEqualTo(Cbu.EXTERNAL_INSTALLMENT_CBU);
    }

    @Test
    void incomeWhenDescriptionIndicatesADeposit() throws Exception {
        when(processedEventGateway.isProcessed(any())).thenReturn(false);
        when(supportedCurrencies.isSupported(any())).thenReturn(true);
        when(categories.findUnassignedCategoryId()).thenReturn(Optional.of(901L));
        when(repo.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentRecordedData deposit = new PaymentRecordedData(42L, "0001112223334445556667",
                new BigDecimal("500000.00"), "ARS", "Loan Deposit: Car", LocalDate.of(2026, 6, 1));

        listener.onPaymentEvent(cloudEvent("evt-income-1", deposit));

        ArgumentCaptor<Transaction> cap = ArgumentCaptor.forClass(Transaction.class);
        verify(repo).save(cap.capture());
        assertThat(cap.getValue().fromCbu()).isEqualTo(Cbu.EXTERNAL_INSTALLMENT_CBU);
        assertThat(cap.getValue().toCbu()).isEqualTo(new Cbu("0001112223334445556667"));
    }

    @Test
    void duplicateCeIdIsSkipped() throws Exception {
        when(processedEventGateway.isProcessed(new EventId("evt-dup-1"))).thenReturn(true);

        listener.onPaymentEvent(cloudEvent("evt-dup-1", loanPayment()));

        verify(repo, never()).save(any());
        verify(processedEventGateway, never()).markProcessed(any());
    }

    @Test
    void rejectsUnsupportedCurrencyAndRecordsNothing() throws Exception {
        when(processedEventGateway.isProcessed(any())).thenReturn(false);
        when(supportedCurrencies.isSupported(Currency.getInstance("JPY"))).thenReturn(false);
        when(supportedCurrencies.all()).thenReturn(Set.of(Currency.getInstance("ARS")));

        PaymentRecordedData jpy = new PaymentRecordedData(42L, "0001112223334445556667",
                new BigDecimal("100.00"), "JPY", "Loan Payment", LocalDate.of(2026, 6, 1));

        assertThatThrownBy(() -> listener.onPaymentEvent(cloudEvent("evt-jpy-1", jpy)))
                .isInstanceOf(UnsupportedCurrencyException.class);

        verify(repo, never()).save(any());
    }

    @Test
    void incomeKeywordsAreTheExpectedSet() {
        assertThat(PaymentEventListener.INCOME_KEYWORDS)
                .containsExactlyInAnyOrder("deposit", "received", "ingreso");
    }
}
