package com.financialapp.finances.infrastructure.messaging.mapper;

import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.common.model.TransactionId;
import com.financialapp.finances.domain.event.TransactionCreated;
import com.financialapp.finances.infrastructure.messaging.payload.TransactionCreatedEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionEventMapperTest {

    private final TransactionEventMapper mapper = new TransactionEventMapper();

    @Test
    void rendersSignedAmountAndAccountCbuWithOutboxIdAsTransactionId() {
        TransactionCreated e = new TransactionCreated(
                new TransactionId(7L), new Cbu("0001112223334445556667"),
                new BigDecimal("-1250.00"), Currency.getInstance("ARS"));

        TransactionCreatedEvent wire = mapper.toWire(e, 9001L);

        assertThat(wire.transactionId()).isEqualTo(9001L);   // outbox row id, NOT the tx id
        assertThat(wire.accountCbu()).isEqualTo("0001112223334445556667");
        assertThat(wire.amount()).isEqualByComparingTo("-1250.00");
        assertThat(wire.currency()).isEqualTo("ARS");
    }
}
