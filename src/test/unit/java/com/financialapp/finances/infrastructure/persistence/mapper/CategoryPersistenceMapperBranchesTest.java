package com.financialapp.finances.infrastructure.persistence.mapper;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.CategoryStatus;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.category.Category;
import com.financialapp.finances.domain.model.category.CategoryName;
import com.financialapp.finances.domain.model.category.Subcategory;
import com.financialapp.finances.infrastructure.persistence.entity.CategoryJpaEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryPersistenceMapperBranchesTest {

    private final CategoryPersistenceMapper mapper = new CategoryPersistenceMapper();

    @Test void newChildEntity_fromActiveSubcategory_isActive() {
        // Given an active reconstituted subcategory
        Subcategory active = Subcategory.reconstitute(new CategoryId(11L), new CategoryName("Dining"), CategoryStatus.ACTIVE);
        // When mapped to a new child row for user 42
        CategoryJpaEntity e = mapper.newChildEntity(active, 42L);
        // Then it has no id, carries the name/user and the active flag (status ACTIVE branch)
        assertThat(e.getId()).isNull();
        assertThat(e.getUserId()).isEqualTo(42L);
        assertThat(e.getName()).isEqualTo("Dining");
        assertThat(e.isActive()).isTrue();
    }

    @Test void newChildEntity_fromArchivedSubcategory_isInactive() {
        // Given an archived subcategory (status ARCHIVED branch)
        Subcategory archived = Subcategory.reconstitute(new CategoryId(12L), new CategoryName("Old"), CategoryStatus.ARCHIVED);
        CategoryJpaEntity e = mapper.newChildEntity(archived, 42L);
        assertThat(e.isActive()).isFalse();
    }

    @Test void newRootEntity_fromArchivedCategory_isInactive() {
        // Given an archived root category (newRootEntity status ARCHIVED branch)
        Category archived = Category.reconstitute(new CategoryId(1L), new UserId(42L),
                new CategoryName("Old"), CategoryStatus.ARCHIVED, List.of());
        CategoryJpaEntity e = mapper.newRootEntity(archived);
        assertThat(e.isActive()).isFalse();
    }
}
