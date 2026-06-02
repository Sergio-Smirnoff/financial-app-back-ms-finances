package com.financialapp.finances.infrastructure.persistence.mapper;

import com.financialapp.finances.domain.common.model.*;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.infrastructure.persistence.entity.TransactionJpaEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionPersistenceMapperTest {

    private final TransactionPersistenceMapper mapper = new TransactionPersistenceMapper();
    private static final Currency ARS = Currency.getInstance("ARS");

    private Transaction sample() {
        return Transaction.create(
                new UserId(42L),
                new Cbu("0001112223334445556667"),
                new Cbu("9998887776665554443332"),
                new Money(new BigDecimal("1250.00"), ARS),
                new CategoryId(5L),
                "Rent",
                LocalDate.of(2026, 6, 1));
    }

    @Test
    void toEntityCopiesEveryField() {
        TransactionJpaEntity e = mapper.toEntity(sample());
        assertThat(e.getUserId()).isEqualTo(42L);
        assertThat(e.getFromCbu()).isEqualTo("0001112223334445556667");
        assertThat(e.getToCbu()).isEqualTo("9998887776665554443332");
        assertThat(e.getAmount()).isEqualByComparingTo("1250.00");
        assertThat(e.getCurrency()).isEqualTo("ARS");
        assertThat(e.getCategoryId()).isEqualTo(5L);
        assertThat(e.getDescription()).isEqualTo("Rent");
        assertThat(e.getDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(e.getCreatedAt()).isNotNull();
        assertThat(e.getUpdatedAt()).isNotNull();
    }

    @Test
    void toDomainReconstitutesWithId() {
        TransactionJpaEntity e = mapper.toEntity(sample());
        e.setId(99L);
        Transaction back = mapper.toDomain(e);
        assertThat(back.id()).isEqualTo(new TransactionId(99L));
        assertThat(back.userId()).isEqualTo(new UserId(42L));
        assertThat(back.fromCbu()).isEqualTo(new Cbu("0001112223334445556667"));
        assertThat(back.money().amount()).isEqualByComparingTo("1250.00");
        assertThat(back.currency()).isEqualTo(ARS);
    }
}
