package com.financialapp.finances.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.common.model.TransactionId;
import com.financialapp.finances.domain.event.TransactionReversed;
import com.financialapp.finances.infrastructure.messaging.mapper.TransactionEventMapper;
import com.financialapp.finances.infrastructure.persistence.entity.OutboxEventJpaEntity;
import com.financialapp.finances.infrastructure.persistence.jpa.OutboxEventJpaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OutboxDomainEventPublisherReversedTest {

    private final OutboxEventJpaRepository repo = mock(OutboxEventJpaRepository.class);
    private final OutboxDomainEventPublisher publisher =
            new OutboxDomainEventPublisher(repo, new TransactionEventMapper(), new ObjectMapper());

    @Test
    void writesAnUnsentRowForAReversal() {
        when(repo.save(any(OutboxEventJpaEntity.class))).thenAnswer(inv -> {
            OutboxEventJpaEntity e = inv.getArgument(0); e.setId(1L); return e;
        });

        publisher.publish(new TransactionReversed(
                new TransactionId(7L), new Cbu("0001112223334445556667"),
                new BigDecimal("100.00"), Currency.getInstance("ARS")));

        ArgumentCaptor<OutboxEventJpaEntity> cap = ArgumentCaptor.forClass(OutboxEventJpaEntity.class);
        verify(repo, times(2)).save(cap.capture());
        OutboxEventJpaEntity row = cap.getValue();
        assertThat(row.getTopic()).isEqualTo("transaction.created");
        assertThat(row.getAggregateKey()).isEqualTo("7");
        assertThat(row.getPayload()).contains("\"amount\":100.00");
    }
}
