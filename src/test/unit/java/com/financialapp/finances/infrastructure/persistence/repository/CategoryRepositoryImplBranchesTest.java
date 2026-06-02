package com.financialapp.finances.infrastructure.persistence.repository;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.CategoryStatus;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.category.Category;
import com.financialapp.finances.domain.model.category.CategoryName;
import com.financialapp.finances.domain.model.category.Subcategory;
import com.financialapp.finances.infrastructure.persistence.entity.CategoryJpaEntity;
import com.financialapp.finances.infrastructure.persistence.jpa.CategoryJpaRepository;
import com.financialapp.finances.infrastructure.persistence.mapper.CategoryPersistenceMapper;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CategoryRepositoryImplBranchesTest {

    private final CategoryJpaRepository jpa = mock(CategoryJpaRepository.class);
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final CategoryRepositoryImpl repo =
            new CategoryRepositoryImpl(jpa, new CategoryPersistenceMapper(), jdbc);

    @Test void save_updatesExistingRoot_reconcilingExistingAndNewChildren() {
        // Given a persisted root whose loaded children include one with an id and one transient
        // (null id) row — the latter exercises the `child.getId() != null` filter's false branch.
        CategoryJpaEntity existingChild = CategoryJpaEntity.builder().id(10L).userId(42L).name("Groceries").active(true).build();
        CategoryJpaEntity transientChild = CategoryJpaEntity.builder().userId(42L).name("Stale").active(true).build();
        CategoryJpaEntity rootRow = CategoryJpaEntity.builder().id(1L).userId(42L).name("Food").active(true)
                .children(new ArrayList<>(List.of(existingChild, transientChild))).build();
        when(jpa.findById(1L)).thenReturn(Optional.of(rootRow));
        when(jpa.save(any(CategoryJpaEntity.class))).thenAnswer(inv -> {
            CategoryJpaEntity root = inv.getArgument(0);
            long next = 20L;
            for (CategoryJpaEntity child : root.getChildren()) {
                if (child.getId() == null) child.setId(next++);
            }
            return root;
        });

        // And an ARCHIVED aggregate (root + existing sub archived) that also adds a brand-new sub —
        // covering the inactive branches of root.setActive / child.setActive in reconcileChildren.
        Category aggregate = Category.reconstitute(new CategoryId(1L), new UserId(42L),
                new CategoryName("Food"), CategoryStatus.ARCHIVED,
                List.of(Subcategory.reconstitute(new CategoryId(10L), new CategoryName("Groceries"), CategoryStatus.ARCHIVED)))
                .addSubcategory(new CategoryName("Dining"));

        // When saved
        repo.save(aggregate);

        // Then the existing child is updated in place and the new one appended (both via the same root row)
        verify(jpa).findById(1L);
        assertThat(rootRow.getChildren()).extracting(CategoryJpaEntity::getName)
                .containsExactly("Groceries", "Dining");
        assertThat(rootRow.getChildren().get(0).getId()).isEqualTo(10L); // existing kept
    }

    @Test void save_throws_whenExistingRootMissing() {
        // Given the root id is not in the db
        when(jpa.findById(99L)).thenReturn(Optional.empty());
        Category aggregate = Category.reconstitute(new CategoryId(99L), new UserId(42L),
                new CategoryName("Food"), CategoryStatus.ACTIVE, List.of());
        // When saving / Then it fails loading the root
        assertThatThrownBy(() -> repo.save(aggregate)).isInstanceOf(IllegalArgumentException.class);
    }
}
