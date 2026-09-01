package com.financialapp.finances.infrastructure.persistence.repository;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.CategoryStatus;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.category.Category;
import com.financialapp.finances.domain.model.category.CategoryNames;
import com.financialapp.finances.domain.model.category.Subcategory;
import com.financialapp.finances.domain.repository.CategoryRepository;
import com.financialapp.finances.infrastructure.persistence.entity.CategoryJpaEntity;
import com.financialapp.finances.infrastructure.persistence.jpa.CategoryJpaRepository;
import com.financialapp.finances.infrastructure.persistence.mapper.CategoryPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

    private static final String NAMES_QUERY =
            "SELECT c.name AS name, p.name AS parent_name " +
            "FROM finances.categories c " +
            "LEFT JOIN finances.categories p ON c.parent_id = p.id " +
            "WHERE c.id = ?";

    private final CategoryJpaRepository jpa;
    private final CategoryPersistenceMapper mapper;
    private final JdbcTemplate jdbcTemplate;
    private final SystemCategoryResolver systemCategoryResolver;

    @Override
    public Optional<CategoryId> findUnassignedCategory() {
        return systemCategoryResolver.findUnassignedCategoryId().map(CategoryId::new);
    }

    @Override
    public Optional<Category> findByIdOwnedBy(CategoryId id, UserId userId) {
        return jpa.findByIdAndUserIdAndParentIsNull(id.value(), userId.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<CategoryNames> findNamesById(CategoryId id) {
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(NAMES_QUERY, id.value());
            String name = (String) row.get("name");
            String parent = (String) row.get("parent_name");
            // parent null → the id is a top-level category: (name, null). Otherwise (parent, name).
            return Optional.of(parent == null
                    ? new CategoryNames(name, null)
                    : new CategoryNames(parent, name));
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    @Override
    public List<Category> findAllOwnedBy(UserId userId) {
        return jpa.findByUserIdAndParentIsNull(userId.value()).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Category save(Category category) {
        CategoryJpaEntity root = (category.id() == null)
                ? mapper.newRootEntity(category)
                : loadRoot(category.id().value());
        root.setName(category.name().text());
        root.setActive(category.status() == CategoryStatus.ACTIVE);
        root.setUserId(category.userId().value());
        root.setChildren(reconcileChildren(category, root));
        return mapper.toDomain(jpa.save(root));
    }

    private CategoryJpaEntity loadRoot(Long id) {
        return jpa.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("category " + id + " not found"));
    }

    /** Rebuild the children list: keep existing rows (updated in place), append newly-added subcategories. */
    private List<CategoryJpaEntity> reconcileChildren(Category category, CategoryJpaEntity root) {
        Map<Long, CategoryJpaEntity> existing = root.getChildren().stream()
                .filter(child -> child.getId() != null)
                .collect(Collectors.toMap(CategoryJpaEntity::getId, Function.identity()));

        List<CategoryJpaEntity> result = new ArrayList<>();
        for (Subcategory subcategory : category.subcategories()) {
            CategoryJpaEntity child = subcategory.id() == null
                    ? mapper.newChildEntity(subcategory, category.userId().value())
                    : existing.get(subcategory.id().value());
            child.setName(subcategory.name().text());
            child.setActive(subcategory.status() == CategoryStatus.ACTIVE);
            child.setParent(root);
            result.add(child);
        }
        return result;
    }
}
