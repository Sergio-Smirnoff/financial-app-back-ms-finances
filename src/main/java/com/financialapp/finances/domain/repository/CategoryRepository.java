package com.financialapp.finances.domain.repository;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.category.Category;

import java.util.List;
import java.util.Optional;

/**
 * Persistence port for the {@link Category} aggregate. Implemented in the infrastructure layer in a
 * later slice. Returns fully reconstituted aggregates; "tree"/"flat" presentation shaping is a
 * read-side query concern, not part of this port. Visibility is ownership — there are no global
 * categories, so a category is visible to a user iff that user owns it.
 */
public interface CategoryRepository {

    Category save(Category category);

    Optional<Category> findByIdOwnedBy(CategoryId id, UserId userId);

    List<Category> findAllOwnedBy(UserId userId);
}
