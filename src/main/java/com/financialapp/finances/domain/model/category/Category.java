package com.financialapp.finances.domain.model.category;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.CategoryStatus;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.exception.category.SubcategoryNotInCategoryException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Category aggregate root. Always owned by a user ({@code userId} is never null — there are no
 * global/system categories). Immutable: {@code create}/{@code reconstitute} build it; every mutator
 * returns a new instance. Subcategories are owned children — all subcategory changes go through the
 * root. Soft-delete is {@link CategoryStatus#ARCHIVED}. Pre-persist instances have a null id.
 */
public final class Category {

    private final CategoryId id;          // null until persisted
    private final UserId userId;
    private final CategoryName name;
    private final CategoryStatus status;
    private final List<Subcategory> subcategories;

    private Category(CategoryId id, UserId userId, CategoryName name,
                     CategoryStatus status, List<Subcategory> subcategories) {
        this.id = id;
        this.userId = Objects.requireNonNull(userId, "userId");
        this.name = Objects.requireNonNull(name, "name");
        this.status = Objects.requireNonNull(status, "status");
        this.subcategories = List.copyOf(subcategories);
    }

    /** New, unpersisted, ACTIVE category with no subcategories. */
    public static Category create(UserId userId, CategoryName name) {
        return new Category(null, userId, name, CategoryStatus.ACTIVE, List.of());
    }

    /** Rehydrate a persisted aggregate from storage (id + stored status, no birth rules). */
    public static Category reconstitute(CategoryId id, UserId userId, CategoryName name,
                                        CategoryStatus status, List<Subcategory> subcategories) {
        return new Category(Objects.requireNonNull(id, "id"), userId, name, status, subcategories);
    }

    public Category rename(CategoryName newName) {
        return new Category(id, userId, newName, status, subcategories);
    }

    public Category archive() {
        return new Category(id, userId, name, CategoryStatus.ARCHIVED, subcategories);
    }

    public Category restore() {
        return new Category(id, userId, name, CategoryStatus.ACTIVE, subcategories);
    }

    public Category addSubcategory(CategoryName subName) {
        List<Subcategory> updated = new ArrayList<>(subcategories);
        updated.add(Subcategory.create(subName));
        return new Category(id, userId, name, status, updated);
    }

    public Category archiveSubcategory(CategoryId subId) {
        List<Subcategory> updated = new ArrayList<>(subcategories.size());
        boolean found = false;
        for (Subcategory sub : subcategories) {
            if (Objects.equals(sub.id(), subId)) {
                updated.add(sub.archive());
                found = true;
            } else {
                updated.add(sub);
            }
        }
        if (!found) {
            throw new SubcategoryNotInCategoryException(id, subId);
        }
        return new Category(id, userId, name, status, updated);
    }

    public CategoryId id() { return id; }
    public UserId userId() { return userId; }
    public CategoryName name() { return name; }
    public CategoryStatus status() { return status; }
    public List<Subcategory> subcategories() { return subcategories; }

    // Identity equality: by persisted id; pre-persist instances (null id) are equal only to self.
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override public int hashCode() {
        return id == null ? System.identityHashCode(this) : id.hashCode();
    }
}
