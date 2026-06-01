package com.financialapp.finances.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.finances.domain.event.DomainEvent;
import com.financialapp.finances.domain.event.TransactionCreated;
import com.financialapp.finances.domain.event.TransactionReversed;
import com.financialapp.finances.domain.gateway.DomainEventPublisher;
import com.financialapp.finances.infrastructure.messaging.mapper.TransactionEventMapper;
import com.financialapp.finances.infrastructure.messaging.payload.TransactionCreatedEvent;
import com.financialapp.finances.infrastructure.persistence.entity.OutboxEventJpaEntity;
import com.financialapp.finances.infrastructure.persistence.jpa.OutboxEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class OutboxDomainEventPublisher implements DomainEventPublisher {

    private static final String TOPIC_TRANSACTION_CREATED = "transaction.created";

    private final OutboxEventJpaRepository outboxRepository;
    private final TransactionEventMapper eventMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(DomainEvent event) {
        if (event instanceof TransactionCreated tc) {
            persist(String.valueOf(tc.sourceTransactionId().value()),
                    rowId -> eventMapper.toWire(tc, rowId));
            return;
        }
        if (event instanceof TransactionReversed tr) {
            persist(String.valueOf(tr.sourceTransactionId().value()),
                    rowId -> eventMapper.reversalTransactionToWire(tr, rowId));
            return;
        }
        throw new IllegalArgumentException("Unsupported domain event: " + event.getClass().getName());
    }

    /** Persist first to obtain the row id, then serialize the wire payload with that id. */
    private void persist(String aggregateKey, Function<Long, TransactionCreatedEvent> toWire) {
        OutboxEventJpaEntity row = outboxRepository.save(OutboxEventJpaEntity.builder()
                .topic(TOPIC_TRANSACTION_CREATED)
                .aggregateKey(aggregateKey)
                .payload("{}")
                .sent(false)
                .build());
        row.setPayload(serialize(toWire.apply(row.getId())));
        outboxRepository.save(row);
    }

    private String serialize(TransactionCreatedEvent wire) {
        try {
            return objectMapper.writeValueAsString(wire);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize outbox payload", ex);
        }
    }
}
