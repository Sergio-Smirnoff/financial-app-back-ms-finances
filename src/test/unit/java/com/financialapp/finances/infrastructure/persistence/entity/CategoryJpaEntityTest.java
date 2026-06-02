package com.financialapp.finances.infrastructure.persistence.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryJpaEntityTest {

    @Test void onCreate_stampsCreatedAndUpdatedTimestamps() {
        // Given a fresh entity with no timestamps
        CategoryJpaEntity entity = CategoryJpaEntity.builder().name("Food").active(true).build();
        assertThat(entity.getCreatedAt()).isNull();
        // When the @PrePersist hook fires
        entity.onCreate();
        // Then both timestamps are set and equal
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isEqualTo(entity.getCreatedAt());
    }

    @Test void onUpdate_advancesUpdatedTimestampOnly() {
        // Given a persisted entity
        CategoryJpaEntity entity = CategoryJpaEntity.builder().name("Food").active(true).build();
        entity.onCreate();
        var created = entity.getCreatedAt();
        // When the @PreUpdate hook fires
        entity.onUpdate();
        // Then updatedAt is set and createdAt is untouched
        assertThat(entity.getUpdatedAt()).isNotNull();
        assertThat(entity.getCreatedAt()).isEqualTo(created);
    }
}
