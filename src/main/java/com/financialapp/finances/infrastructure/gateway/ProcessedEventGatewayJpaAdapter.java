package com.financialapp.finances.infrastructure.gateway;

import com.financialapp.commons.messaging.domain.gateway.ProcessedEventGateway;
import com.financialapp.commons.messaging.domain.model.EventId;
import com.financialapp.finances.infrastructure.persistence.entity.ProcessedInboundEventJpaEntity;
import com.financialapp.finances.infrastructure.persistence.jpa.ProcessedInboundEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProcessedEventGatewayJpaAdapter implements ProcessedEventGateway {

    private final ProcessedInboundEventJpaRepository repository;

    @Override
    public boolean isProcessed(EventId eventId) {
        return repository.existsById(eventId.value());
    }

    @Override
    public void markProcessed(EventId eventId) {
        repository.save(ProcessedInboundEventJpaEntity.builder()
                .dedupKey(eventId.value())
                .build());
    }
}
