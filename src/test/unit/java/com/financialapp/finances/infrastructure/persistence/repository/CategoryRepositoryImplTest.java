package com.financialapp.finances.infrastructure.persistence.repository;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.category.Category;
import com.financialapp.finances.domain.model.category.CategoryName;
import com.financialapp.finances.domain.model.category.CategoryNames;
import com.financialapp.finances.infrastructure.persistence.entity.CategoryJpaEntity;
import com.financialapp.finances.infrastructure.persistence.jpa.CategoryJpaRepository;
import com.financialapp.finances.infrastructure.persistence.mapper.CategoryPersistenceMapper;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CategoryRepositoryImplTest {

    private final CategoryJpaRepository jpa = mock(CategoryJpaRepository.class);
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final CategoryRepositoryImpl repo =
            new CategoryRepositoryImpl(jpa, new CategoryPersistenceMapper(), jdbc);

    @Test
    void findAllOwnedByMapsRootsWithChildren() {
        CategoryJpaEntity child = CategoryJpaEntity.builder().id(10L).userId(42L).name("Groceries").active(true).build();
        CategoryJpaEntity root = CategoryJpaEntity.builder().id(1L).userId(42L).name("Food").active(true)
                .children(List.of(child)).build();
        when(jpa.findByUserIdAndParentIsNull(42L)).thenReturn(List.of(root));

        List<Category> result = repo.findAllOwnedBy(new UserId(42L));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).subcategories()).extracting(s -> s.name().text()).containsExactly("Groceries");
    }

    @Test
    void findByIdOwnedByEmptyWhenAbsent() {
        when(jpa.findByIdAndUserIdAndParentIsNull(9L, 42L)).thenReturn(Optional.empty());
        assertThat(repo.findByIdOwnedBy(new CategoryId(9L), new UserId(42L))).isEmpty();
    }

    @Test
    void findNamesByIdResolvesSubcategoryToParentAndOwnName() {
        when(jdbc.queryForMap(anyString(), eq(5L)))
                .thenReturn(Map.of("name", "Rent", "parent_name", "Housing"));
        assertThat(repo.findNamesById(new CategoryId(5L)))
                .contains(new CategoryNames("Housing", "Rent"));
    }

    @Test
    void findNamesByIdResolvesRootToCategoryOnly() {
        Map<String, Object> row = new HashMap<>();
        row.put("name", "Housing");
        row.put("parent_name", null);
        when(jdbc.queryForMap(anyString(), eq(1L))).thenReturn(row);
        assertThat(repo.findNamesById(new CategoryId(1L)))
                .contains(new CategoryNames("Housing", null));
    }

    @Test
    void findNamesByIdEmptyWhenUnknown() {
        when(jdbc.queryForMap(anyString(), eq(99L)))
                .thenThrow(new EmptyResultDataAccessException(1));
        assertThat(repo.findNamesById(new CategoryId(99L))).isEmpty();
    }

    @Test
    void saveNewCategoryCascadesRootAndChild() {
        when(jpa.save(any(CategoryJpaEntity.class))).thenAnswer(inv -> {
            CategoryJpaEntity root = inv.getArgument(0);
            if (root.getId() == null) root.setId(1L);
            long next = 10L;
            for (CategoryJpaEntity child : root.getChildren()) {
                if (child.getId() == null) child.setId(next++);
            }
            return root;
        });

        Category toSave = Category.create(new UserId(42L), new CategoryName("Food"))
                .addSubcategory(new CategoryName("Groceries"));
        Category saved = repo.save(toSave);

        assertThat(saved.id().value()).isEqualTo(1L);
        assertThat(saved.subcategories()).extracting(s -> s.id().value()).containsExactly(10L);
        verify(jpa, times(1)).save(any(CategoryJpaEntity.class));
    }
}
