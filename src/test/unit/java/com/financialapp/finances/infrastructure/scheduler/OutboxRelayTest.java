package com.financialapp.finances.infrastructure.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.finances.infrastructure.messaging.payload.TransactionCreatedEvent;
import com.financialapp.finances.infrastructure.persistence.entity.OutboxEventJpaEntity;
import com.financialapp.finances.infrastructure.persistence.jpa.OutboxEventJpaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Limit;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OutboxRelayTest {

    private final OutboxEventJpaRepository repo = mock(OutboxEventJpaRepository.class);
    @SuppressWarnings("unchecked")
    private final KafkaTemplate<String, Object> kafka = mock(KafkaTemplate.class);
    private final OutboxRelay relay = new OutboxRelay(repo, kafka, new ObjectMapper(), 100);

    private OutboxEventJpaEntity row(long id) {
        String payload = "{\"transactionId\":" + id
                + ",\"accountCbu\":\"0001112223334445556667\",\"amount\":-100.00,\"currency\":\"ARS\"}";
        return OutboxEventJpaEntity.builder().id(id).topic("transaction.created")
                .aggregateKey("7").payload(payload).sent(false).build();
    }

    @Test
    void sendsUnsentRowsAsDeserializedEventsThenMarksThemSent() {
        when(repo.findBySentFalseOrderByIdAsc(any(Limit.class)))
                .thenReturn(List.of(row(1L), row(2L)));

        relay.flush();

        ArgumentCaptor<TransactionCreatedEvent> cap = ArgumentCaptor.forClass(TransactionCreatedEvent.class);
        verify(kafka, times(2)).send(eq("transaction.created"), eq("7"), cap.capture());
        assertThat(cap.getAllValues()).extracting(TransactionCreatedEvent::transactionId)
                .containsExactly(1L, 2L);
        verify(repo, times(2)).save(argThat(OutboxEventJpaEntity::isSent));
    }

    @Test
    void aFailedSendLeavesTheRowUnsent() {
        when(repo.findBySentFalseOrderByIdAsc(any(Limit.class))).thenReturn(List.of(row(1L)));
        doThrow(new RuntimeException("broker down"))
                .when(kafka).send(anyString(), anyString(), any());

        relay.flush();

        verify(repo, never()).save(argThat(OutboxEventJpaEntity::isSent));
    }
}
