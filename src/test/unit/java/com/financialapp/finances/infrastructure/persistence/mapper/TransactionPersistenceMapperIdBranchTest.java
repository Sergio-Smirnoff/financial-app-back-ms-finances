package com.financialapp.finances.infrastructure.persistence.mapper;

import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.Money;
import com.financialapp.finances.domain.common.model.TransactionId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.infrastructure.persistence.entity.TransactionJpaEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionPersistenceMapperIdBranchTest {

    private final TransactionPersistenceMapper mapper = new TransactionPersistenceMapper();

    @Test void toEntity_carriesId_whenTransactionPersisted() {
        // Given a reconstituted (persisted, non-null id) transaction
        Transaction persisted = Transaction.reconstitute(new TransactionId(77L), new UserId(42L),
                new Cbu("0001112223334445556667"), new Cbu("9998887776665554443332"),
                new Money(new BigDecimal("100.00"), Currency.getInstance("ARS")),
                new CategoryId(5L), "Rent", LocalDate.of(2026, 6, 1));
        // When mapped to an entity / Then the id is propagated (non-null branch)
        TransactionJpaEntity e = mapper.toEntity(persisted);
        assertThat(e.getId()).isEqualTo(77L);
    }
}
