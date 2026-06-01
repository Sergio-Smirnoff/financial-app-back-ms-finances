package com.financialapp.finances.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.finances.domain.common.model.*;
import com.financialapp.finances.domain.event.DomainEvent;
import com.financialapp.finances.domain.event.TransactionCreated;
import com.financialapp.finances.domain.model.transaction.BalanceMovement;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.service.TransactionPosting;
import com.financialapp.finances.infrastructure.messaging.OutboxDomainEventPublisher;
import com.financialapp.finances.infrastructure.messaging.mapper.TransactionEventMapper;
import com.financialapp.finances.infrastructure.persistence.entity.OutboxEventJpaEntity;
import com.financialapp.finances.infrastructure.persistence.jpa.OutboxEventJpaRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransferOutboxFanOutTest {

    private static final Currency ARS = Currency.getInstance("ARS");

    @Test
    void transferProducesTwoWireEventsWithDistinctIdsSignedPerAccount() throws Exception {
        Cbu from = new Cbu("0001112223334445556667");
        Cbu to = new Cbu("9998887776665554443332");
        Transaction tx = Transaction.reconstitute(new TransactionId(7L), new UserId(42L), from, to,
                new Money(new BigDecimal("100.00"), ARS), new CategoryId(5L), "move",
                LocalDate.of(2026, 6, 1));

        List<BalanceMovement> movements = new TransactionPosting().post(tx, Set.of(from, to));
        assertThat(movements).hasSize(2);

        // Outbox repo that assigns sequential ids and keeps the saved rows.
        OutboxEventJpaRepository repo = mock(OutboxEventJpaRepository.class);
        List<OutboxEventJpaEntity> stored = new ArrayList<>();
        AtomicLong seq = new AtomicLong(9000);
        when(repo.save(any(OutboxEventJpaEntity.class))).thenAnswer(inv -> {
            OutboxEventJpaEntity e = inv.getArgument(0);
            if (e.getId() == null) { e.setId(seq.incrementAndGet()); stored.add(e); }
            return e;
        });

        ObjectMapper om = new ObjectMapper();
        OutboxDomainEventPublisher publisher =
                new OutboxDomainEventPublisher(repo, new TransactionEventMapper(), om);

        List<DomainEvent> events = movements.stream()
                .map(m -> (DomainEvent) new TransactionCreated(tx.id(), m.account(), m.signedAmount(), m.currency()))
                .toList();
        publisher.publishAll(events);

        assertThat(stored).hasSize(2);
        JsonNode a = om.readTree(stored.get(0).getPayload());
        JsonNode b = om.readTree(stored.get(1).getPayload());

        // Distinct idempotency ids
        assertThat(a.get("transactionId").asLong()).isNotEqualTo(b.get("transactionId").asLong());
        // Signed per account: from = -100, to = +100
        assertThat(a.get("accountCbu").asText()).isEqualTo("0001112223334445556667");
        assertThat(a.get("amount").decimalValue()).isEqualByComparingTo("-100.00");
        assertThat(b.get("accountCbu").asText()).isEqualTo("9998887776665554443332");
        assertThat(b.get("amount").decimalValue()).isEqualByComparingTo("100.00");
    }
}
