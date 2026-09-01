package com.financialapp.finances.infrastructure.messaging.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.commons.messaging.domain.model.OutboxRecord;
import com.financialapp.commons.core.domain.model.Cbu;
import com.financialapp.finances.domain.common.model.TransactionId;
import com.financialapp.finances.domain.event.TransactionReversed;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionReversedMapperTest {

    private final TransactionReversedMapper mapper = new TransactionReversedMapper(new ObjectMapper());

    @Test
    void producesCorrectOutboxRecordForReversal() {
        TransactionReversed event = new TransactionReversed(
                new TransactionId(7L), new Cbu("0001112223334445556667"),
                new BigDecimal("1250.00"), Currency.getInstance("ARS"));

        List<OutboxRecord> records = mapper.toOutboxRecords(event);

        assertThat(records).hasSize(1);
        OutboxRecord r = records.get(0);
        assertThat(r.topic()).isEqualTo("finances.transaction.created");
        assertThat(r.type().value()).isEqualTo("finances.transaction.created");
        assertThat(r.key()).isEqualTo("7");
        assertThat(r.source()).isEqualTo("ms-finances");
        assertThat(r.eventId()).isNotNull();
        assertThat(r.dataJson()).contains("\"transactionId\":7");
        assertThat(r.dataJson()).contains("\"accountCbu\":\"0001112223334445556667\"");
        assertThat(r.dataJson()).contains("\"amount\":1250.00");
        assertThat(r.dataJson()).contains("\"currency\":\"ARS\"");
    }

    @Test
    void supportsOnlyTransactionReversedEvent() {
        assertThat(mapper.supports(new Object())).isFalse();
        assertThat(mapper.supports(new TransactionReversed(
                new TransactionId(1L), new Cbu("0001112223334445556667"),
                new BigDecimal("100.00"), Currency.getInstance("ARS")))).isTrue();
    }
}
