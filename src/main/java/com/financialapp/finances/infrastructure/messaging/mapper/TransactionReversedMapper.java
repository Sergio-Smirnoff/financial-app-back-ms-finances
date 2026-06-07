package com.financialapp.finances.infrastructure.messaging.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.commons.messaging.domain.gateway.TypedDomainEventMapper;
import com.financialapp.commons.messaging.domain.model.EventType;
import com.financialapp.commons.messaging.domain.model.OutboxRecord;
import com.financialapp.finances.domain.event.TransactionReversed;
import com.financialapp.finances.infrastructure.messaging.payload.TransactionCreatedData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TransactionReversedMapper extends TypedDomainEventMapper<TransactionReversed> {

    private final ObjectMapper objectMapper;

    public TransactionReversedMapper(ObjectMapper objectMapper) {
        super(TransactionReversed.class);
        this.objectMapper = objectMapper;
    }

    @Override
    protected List<OutboxRecord> mapTyped(TransactionReversed event) {
        TransactionCreatedData data = new TransactionCreatedData(
                event.sourceTransactionId().value(),
                event.accountCbu().cbuNumber(),
                event.signedAmount(),
                event.currency().getCurrencyCode());
        return List.of(OutboxRecord.create(
                TransactionCreatedMapper.TOPIC,
                String.valueOf(event.sourceTransactionId().value()),
                new EventType(TransactionCreatedMapper.TOPIC),
                TransactionCreatedMapper.SOURCE,
                TransactionCreatedMapper.SCHEMA,
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
