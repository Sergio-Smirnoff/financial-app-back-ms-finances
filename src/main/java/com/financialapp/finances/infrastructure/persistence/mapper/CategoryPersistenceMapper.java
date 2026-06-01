package com.financialapp.finances.infrastructure.persistence.mapper;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.CategoryStatus;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.category.Category;
import com.financialapp.finances.domain.model.category.CategoryName;
import com.financialapp.finances.domain.model.category.Subcategory;
import com.financialapp.finances.infrastructure.persistence.entity.CategoryJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/** Maps between the {@link Category} aggregate and {@code categories} rows (root + its children). */
@Component
public class CategoryPersistenceMapper {

    public Category toDomain(CategoryJpaEntity root) {
        List<Subcategory> subcategories = root.getChildren().stream().map(this::toSubcategory).toList();
        return Category.reconstitute(
                new CategoryId(root.getId()), new UserId(root.getUserId()),
                new CategoryName(root.getName()), status(root.isActive()), subcategories);
    }

    public Subcategory toSubcategory(CategoryJpaEntity child) {
        return Subcategory.reconstitute(
                new CategoryId(child.getId()), new CategoryName(child.getName()), status(child.isActive()));
    }

    public CategoryJpaEntity newRootEntity(Category category) {
        return CategoryJpaEntity.builder()
                .userId(category.userId().value())
                .name(category.name().value())
                .active(category.status() == CategoryStatus.ACTIVE)
                .build();
    }

    public CategoryJpaEntity newChildEntity(Subcategory subcategory, Long userId) {
        return CategoryJpaEntity.builder()
                .userId(userId)
                .name(subcategory.name().value())
                .active(subcategory.status() == CategoryStatus.ACTIVE)
                .build();
    }

    private CategoryStatus status(boolean active) {
        return active ? CategoryStatus.ACTIVE : CategoryStatus.ARCHIVED;
    }
}
