package com.financialapp.finances.infrastructure.messaging.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.commons.messaging.domain.gateway.TypedDomainEventMapper;

abstract class JsonTypedDomainEventMapper<E> extends TypedDomainEventMapper<E> {

    private final ObjectMapper objectMapper;

    protected JsonTypedDomainEventMapper(Class<E> eventType, ObjectMapper objectMapper) {
        super(eventType);
        this.objectMapper = objectMapper;
    }

    protected String serialize(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize event data", ex);
        }
    }
}
