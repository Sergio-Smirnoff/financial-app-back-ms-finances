package com.financialapp.finances.infrastructure.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.finances.infrastructure.messaging.payload.TransactionCreatedEvent;
import com.financialapp.finances.infrastructure.persistence.entity.OutboxEventJpaEntity;
import com.financialapp.finances.infrastructure.persistence.jpa.OutboxEventJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Limit;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Ships unsent outbox rows to Kafka. The stored payload is the serialized
 * {@link TransactionCreatedEvent}; it is deserialized back to the record and sent through the
 * shared {@code KafkaTemplate} (JsonSerializer) — the same proven path ms-banks already consumes,
 * which avoids the double-encoding a raw-String send would cause.
 */
@Component
@Slf4j
public class OutboxRelay {

    private final OutboxEventJpaRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper eventPayloadMapper;
    private final int batchSize;

    public OutboxRelay(OutboxEventJpaRepository outboxRepository,
                       KafkaTemplate<String, Object> kafkaTemplate,
                       ObjectMapper eventPayloadMapper,
                       @Value("${finances.outbox.batch-size:100}") int batchSize) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.eventPayloadMapper = eventPayloadMapper;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${finances.outbox.poll-ms:2000}")
    @Transactional
    public void flush() {
        List<OutboxEventJpaEntity> batch =
                outboxRepository.findBySentFalseOrderByIdAsc(Limit.of(batchSize));
        for (OutboxEventJpaEntity row : batch) {
            try {
                TransactionCreatedEvent payload =
                        eventPayloadMapper.readValue(row.getPayload(), TransactionCreatedEvent.class);
                kafkaTemplate.send(row.getTopic(), row.getAggregateKey(), payload);
                row.setSent(true);
                row.setSentAt(LocalDateTime.now());
                outboxRepository.save(row);
            } catch (Exception ex) {
                // Leave sent=false; next sweep retries. ms-banks dedups any duplicate.
                log.error("Outbox relay failed for row {}: {}", row.getId(), ex.getMessage());
            }
        }
    }
}
