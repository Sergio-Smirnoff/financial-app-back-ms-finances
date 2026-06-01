package com.financialapp.finances.infrastructure.persistence.mapper;

import com.financialapp.finances.domain.common.model.CategoryStatus;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.category.Category;
import com.financialapp.finances.domain.model.category.CategoryName;
import com.financialapp.finances.infrastructure.persistence.entity.CategoryJpaEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryPersistenceMapperTest {

    private final CategoryPersistenceMapper mapper = new CategoryPersistenceMapper();

    private CategoryJpaEntity child(Long id, String name, boolean active) {
        return CategoryJpaEntity.builder().id(id).userId(42L).name(name).active(active).build();
    }

    @Test
    void toDomainAssemblesRootWithChildren() {
        CategoryJpaEntity root = CategoryJpaEntity.builder()
                .id(1L).userId(42L).name("Food").active(true)
                .children(List.of(child(10L, "Groceries", true), child(11L, "Old", false)))
                .build();

        Category c = mapper.toDomain(root);

        assertThat(c.id().value()).isEqualTo(1L);
        assertThat(c.name().value()).isEqualTo("Food");
        assertThat(c.status()).isEqualTo(CategoryStatus.ACTIVE);
        assertThat(c.subcategories()).extracting(s -> s.name().value()).containsExactly("Groceries", "Old");
        assertThat(c.subcategories().get(1).status()).isEqualTo(CategoryStatus.ARCHIVED);
    }

    @Test
    void newRootEntityHasNoIdNoParentAndActiveFlagFromStatus() {
        Category c = Category.create(new UserId(42L), new CategoryName("Food"));
        CategoryJpaEntity e = mapper.newRootEntity(c);
        assertThat(e.getId()).isNull();
        assertThat(e.getParent()).isNull();
        assertThat(e.getUserId()).isEqualTo(42L);
        assertThat(e.getName()).isEqualTo("Food");
        assertThat(e.isActive()).isTrue();
    }
}
