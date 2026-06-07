package com.financialapp.finances.infrastructure.gateway;

import com.financialapp.commons.messaging.domain.model.EventId;
import com.financialapp.commons.messaging.domain.model.EventType;
import com.financialapp.commons.messaging.domain.model.OutboxRecord;
import com.financialapp.finances.infrastructure.persistence.entity.OutboxEventJpaEntity;
import com.financialapp.finances.infrastructure.persistence.jpa.OutboxEventJpaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Limit;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OutboxGatewayJpaAdapterTest {

    private final OutboxEventJpaRepository repo = mock(OutboxEventJpaRepository.class);
    private final OutboxGatewayJpaAdapter adapter = new OutboxGatewayJpaAdapter(repo);

    private OutboxRecord record() {
        return OutboxRecord.create(
                "finances.transaction.created", "7",
                new EventType("finances.transaction.created"),
                "ms-finances",
                "https://schemas.financial-app/finances/transaction-created/v1",
                "{\"transactionId\":7}");
    }

    @Test
    void saveMapsAllFieldsToEntity() {
        OutboxRecord r = record();
        adapter.save(r);

        ArgumentCaptor<OutboxEventJpaEntity> cap = ArgumentCaptor.forClass(OutboxEventJpaEntity.class);
        verify(repo).save(cap.capture());
        OutboxEventJpaEntity entity = cap.getValue();
        assertThat(entity.getEventId()).isEqualTo(r.eventId().value());
        assertThat(entity.getTopic()).isEqualTo("finances.transaction.created");
        assertThat(entity.getAggregateKey()).isEqualTo("7");
        assertThat(entity.getCeType()).isEqualTo("finances.transaction.created");
        assertThat(entity.getCeSource()).isEqualTo("ms-finances");
        assertThat(entity.isSent()).isFalse();
    }

    @Test
    void findUnsentMapsEntitiesToRecords() {
        OutboxEventJpaEntity entity = new OutboxEventJpaEntity();
        entity.setEventId("test-id");
        entity.setTopic("finances.transaction.created");
        entity.setAggregateKey("7");
        entity.setCeType("finances.transaction.created");
        entity.setCeSource("ms-finances");
        entity.setDataSchema("https://schemas.financial-app/finances/transaction-created/v1");
        entity.setDataJson("{\"transactionId\":7}");
        entity.setSent(false);

        when(repo.findBySentFalseOrderByIdAsc(any(Limit.class))).thenReturn(List.of(entity));

        List<OutboxRecord> records = adapter.findUnsent(10);

        assertThat(records).hasSize(1);
        assertThat(records.get(0).eventId().value()).isEqualTo("test-id");
        assertThat(records.get(0).topic()).isEqualTo("finances.transaction.created");
    }

    @Test
    void markSentUpdatesEntity() {
        OutboxEventJpaEntity entity = new OutboxEventJpaEntity();
        entity.setSent(false);
        when(repo.findByEventId("test-id")).thenReturn(Optional.of(entity));

        adapter.markSent(new EventId("test-id"));

        assertThat(entity.isSent()).isTrue();
        assertThat(entity.getSentAt()).isNotNull();
        verify(repo).save(entity);
    }
}
