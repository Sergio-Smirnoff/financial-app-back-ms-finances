package com.financialapp.finances.infrastructure.messaging.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.commons.messaging.domain.gateway.TypedDomainEventMapper;
import com.financialapp.commons.messaging.domain.model.EventType;
import com.financialapp.commons.messaging.domain.model.OutboxRecord;
import com.financialapp.finances.domain.event.TransactionCreated;
import com.financialapp.finances.infrastructure.messaging.payload.TransactionCreatedData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TransactionCreatedMapper extends TypedDomainEventMapper<TransactionCreated> {

    static final String TOPIC = "finances.transaction.created";
    static final String SCHEMA = "https://schemas.financial-app/finances/transaction-created/v1";
    static final String SOURCE = "ms-finances";

    private final ObjectMapper objectMapper;

    public TransactionCreatedMapper(ObjectMapper objectMapper) {
        super(TransactionCreated.class);
        this.objectMapper = objectMapper;
    }

    @Override
    protected List<OutboxRecord> mapTyped(TransactionCreated event) {
        TransactionCreatedData data = new TransactionCreatedData(
                event.sourceTransactionId().value(),
                event.accountCbu().cbuNumber(),
                event.signedAmount(),
                event.currency().getCurrencyCode());
        return List.of(OutboxRecord.create(
                TOPIC,
                String.valueOf(event.sourceTransactionId().value()),
                new EventType(TOPIC),
                SOURCE,
                SCHEMA,
                serialize(data)));
    }

    private String serialize(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize event data", ex);
        }
    }
}
