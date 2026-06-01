package com.financialapp.finances.infrastructure.messaging.mapper;

import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.common.model.TransactionId;
import com.financialapp.finances.domain.event.TransactionReversed;
import com.financialapp.finances.infrastructure.messaging.payload.TransactionCreatedEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionEventMapperReversedTest {

    private final TransactionEventMapper mapper = new TransactionEventMapper();

    @Test
    void mapsReversedToWireWithItsSignedAmountAndOutboxId() {
        TransactionReversed e = new TransactionReversed(
                new TransactionId(7L), new Cbu("0001112223334445556667"),
                new BigDecimal("100.00"), Currency.getInstance("ARS"));

        TransactionCreatedEvent wire = mapper.reversalTransactionToWire(e, 9002L);

        assertThat(wire.transactionId()).isEqualTo(9002L);
        assertThat(wire.accountCbu()).isEqualTo("0001112223334445556667");
        assertThat(wire.amount()).isEqualByComparingTo("100.00");
        assertThat(wire.currency()).isEqualTo("ARS");
    }
}
