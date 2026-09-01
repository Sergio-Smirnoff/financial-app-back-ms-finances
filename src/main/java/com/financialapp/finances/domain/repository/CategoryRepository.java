package com.financialapp.finances.domain.repository;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.category.Category;
import com.financialapp.finances.domain.model.category.CategoryNames;

import java.util.List;
import java.util.Optional;

/**
 * Persistence port for the {@link Category} aggregate. Implemented in the infrastructure layer.
 * Returns fully reconstituted aggregates; "tree"/"flat" presentation shaping is a
 * read-side query concern, not part of this port. Visibility is ownership — there are no global
 * categories, so a category is visible to a user iff that user owns it.
 */
public interface CategoryRepository {

    Category save(Category category);

    Optional<Category> findByIdOwnedBy(CategoryId id, UserId userId);

    List<Category> findAllOwnedBy(UserId userId);

    /**
     * Resolve a category-or-subcategory id to display names for read views. Unscoped by owner:
     * the ms-banks account view is a cross-owner callback with no userId. Empty if id is unknown.
     */
    Optional<CategoryNames> findNamesById(CategoryId id);

    /** Resolve the system 'Unassigned' category id. */
    Optional<CategoryId> findUnassignedCategory();
}
