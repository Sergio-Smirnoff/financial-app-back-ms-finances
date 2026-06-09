package com.financialapp.finances.infrastructure.messaging;

import com.financialapp.commons.messaging.infrastructure.messaging.relay.OutboxEventPublisher;
import com.financialapp.finances.domain.event.DomainEvent;
import com.financialapp.finances.domain.gateway.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxDomainEventPublisher implements DomainEventPublisher {

    private final OutboxEventPublisher outboxEventPublisher;

    @Override
    public void publish(DomainEvent event) {
        outboxEventPublisher.publish(event);
    }
}
