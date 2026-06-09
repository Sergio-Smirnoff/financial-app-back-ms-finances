package com.financialapp.finances.infrastructure.gateway;

import com.financialapp.commons.messaging.domain.model.EventId;
import com.financialapp.finances.infrastructure.persistence.entity.ProcessedInboundEventJpaEntity;
import com.financialapp.finances.infrastructure.persistence.jpa.ProcessedInboundEventJpaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProcessedEventGatewayJpaAdapterTest {

    private final ProcessedInboundEventJpaRepository repo = mock(ProcessedInboundEventJpaRepository.class);
    private final ProcessedEventGatewayJpaAdapter adapter = new ProcessedEventGatewayJpaAdapter(repo);

    @Test
    void isProcessedDelegatesToExistsById() {
        when(repo.existsById("ce-uuid-123")).thenReturn(true);
        assertThat(adapter.isProcessed(new EventId("ce-uuid-123"))).isTrue();
    }

    @Test
    void isProcessedReturnsFalseForUnknownId() {
        when(repo.existsById("ce-uuid-456")).thenReturn(false);
        assertThat(adapter.isProcessed(new EventId("ce-uuid-456"))).isFalse();
    }

    @Test
    void markProcessedSavesEntityWithDedupKeySetToEventIdValue() {
        adapter.markProcessed(new EventId("ce-uuid-789"));

        ArgumentCaptor<ProcessedInboundEventJpaEntity> cap =
                ArgumentCaptor.forClass(ProcessedInboundEventJpaEntity.class);
        verify(repo).save(cap.capture());
        assertThat(cap.getValue().getDedupKey()).isEqualTo("ce-uuid-789");
    }
}
