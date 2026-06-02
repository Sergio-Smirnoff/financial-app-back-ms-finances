package com.financialapp.finances.web.mapper;

import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.usecase.transaction.command.RecordTransactionCommand;
import com.financialapp.finances.web.dto.request.RecordTransactionRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionWebMapperToCommandTest {

    private final TransactionWebMapper mapper = new TransactionWebMapper();

    @Test void toRecordCommand_parsesRequestIntoDomainCommand() {
        // Given a record-transaction request with money as decimal strings
        RecordTransactionRequest req = new RecordTransactionRequest(
                "0001112223334445556667", "9998887776665554443332",
                "1250.50", "ARS", 5L, "Rent", LocalDate.of(2026, 6, 1));
        // When mapped for user 42
        RecordTransactionCommand cmd = mapper.toRecordCommand(new UserId(42L), req);
        // Then every field is reified into the value objects
        assertThat(cmd.userId()).isEqualTo(new UserId(42L));
        assertThat(cmd.fromCbu()).isEqualTo(new Cbu("0001112223334445556667"));
        assertThat(cmd.toCbu()).isEqualTo(new Cbu("9998887776665554443332"));
        assertThat(cmd.money().amount()).isEqualByComparingTo("1250.50");
        assertThat(cmd.money().currency().getCurrencyCode()).isEqualTo("ARS");
        assertThat(cmd.categoryId().value()).isEqualTo(5L);
        assertThat(cmd.description()).isEqualTo("Rent");
        assertThat(cmd.date()).isEqualTo(LocalDate.of(2026, 6, 1));
    }
}
