package com.financialapp.finances.domain.gateway;

import com.financialapp.finances.domain.event.DomainEvent;

import java.util.List;

/**
 * Outbound seam: hand recorded domain events to the messaging layer. The implementation is an
 * outbox-backed adapter (writes a row per event inside the current transaction); the domain and
 * application never mention "outbox".
 */
public interface DomainEventPublisher {
    void publish(DomainEvent event);

    /** Publish every event of the operation, in order. */
    default void publishAll(List<DomainEvent> events) {
        events.forEach(this::publish);
    }
}
