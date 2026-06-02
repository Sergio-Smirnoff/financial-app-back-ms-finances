package com.financialapp.finances.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.common.model.TransactionId;
import com.financialapp.finances.domain.event.DomainEvent;
import com.financialapp.finances.domain.event.TransactionCreated;
import com.financialapp.finances.infrastructure.messaging.mapper.TransactionEventMapper;
import com.financialapp.finances.infrastructure.messaging.payload.TransactionCreatedEvent;
import com.financialapp.finances.infrastructure.persistence.entity.OutboxEventJpaEntity;
import com.financialapp.finances.infrastructure.persistence.jpa.OutboxEventJpaRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OutboxDomainEventPublisherBranchesTest {

    private final OutboxEventJpaRepository repo = mock(OutboxEventJpaRepository.class);

    /** A domain event the publisher does not know how to serialize. */
    private record UnknownEvent() implements DomainEvent {}

    @Test void publish_throws_whenEventTypeUnsupported() {
        // Given an unsupported domain event
        OutboxDomainEventPublisher publisher =
                new OutboxDomainEventPublisher(repo, new TransactionEventMapper(), new ObjectMapper());
        // When / Then it is rejected and nothing is persisted
        assertThatThrownBy(() -> publisher.publish(new UnknownEvent()))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(repo);
    }

    @Test void publish_wrapsSerializationFailure() throws JsonProcessingException {
        // Given an ObjectMapper that cannot serialize the wire payload
        ObjectMapper failing = mock(ObjectMapper.class);
        when(failing.writeValueAsString(any(TransactionCreatedEvent.class)))
                .thenThrow(new JsonProcessingException("boom") {});
        when(repo.save(any(OutboxEventJpaEntity.class))).thenAnswer(inv -> {
            OutboxEventJpaEntity e = inv.getArgument(0); e.setId(1L); return e;
        });
        OutboxDomainEventPublisher publisher =
                new OutboxDomainEventPublisher(repo, new TransactionEventMapper(), failing);
        // When publishing / Then the checked failure surfaces as an IllegalStateException
        assertThatThrownBy(() -> publisher.publish(new TransactionCreated(
                new TransactionId(7L), new Cbu("0001112223334445556667"),
                new BigDecimal("-1250.00"), Currency.getInstance("ARS"))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("serialize");
    }
}
