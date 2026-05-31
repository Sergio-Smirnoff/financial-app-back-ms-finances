package com.financialapp.finances.domain.common.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransferGroupIdTest {

    @Test void newIdMintsDistinctNonNullValues() {
        TransferGroupId a = TransferGroupId.newId();
        TransferGroupId b = TransferGroupId.newId();
        assertThat(a.id()).isNotNull();
        assertThat(a).isNotEqualTo(b);
    }

    @Test void ofRehydratesAnExistingUuid() {
        UUID uuid = UUID.randomUUID();
        assertThat(new TransferGroupId(uuid).id()).isEqualTo(uuid);
    }

    @Test void rejectsNull() {
        assertThatThrownBy(() -> new TransferGroupId(null))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
