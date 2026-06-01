package com.financialapp.finances.domain.model.category;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.CategoryStatus;

import java.util.Objects;

/**
 * Subcategory entity, owned by a {@link Category} aggregate root. Immutable; the root is the only
 * thing that creates or changes it. Identity (when persisted) is its {@link CategoryId} — it shares
 * the {@code categories} table id-space with its parent. Pre-persist instances have a null id.
 */
public final class Subcategory {

    private final CategoryId id;          // null until persisted
    private final CategoryName name;
    private final CategoryStatus status;

    private Subcategory(CategoryId id, CategoryName name, CategoryStatus status) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "name");
        this.status = Objects.requireNonNull(status, "status");
    }

    /** New, unpersisted subcategory (ACTIVE, no id). Only a {@link Category} root calls this. */
    static Subcategory create(CategoryName name) {
        return new Subcategory(null, name, CategoryStatus.ACTIVE);
    }

    /** Rehydrate a persisted subcategory from storage (id + stored status, no birth rules). */
    public static Subcategory reconstitute(CategoryId id, CategoryName name, CategoryStatus status) {
        return new Subcategory(Objects.requireNonNull(id, "id"), name, status);
    }

    Subcategory archive() {
        return new Subcategory(id, name, CategoryStatus.ARCHIVED);
    }

    public CategoryId id() { return id; }
    public CategoryName name() { return name; }
    public CategoryStatus status() { return status; }

    // Identity equality: by persisted id; pre-persist instances (null id) are equal only to self.
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subcategory other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override public int hashCode() {
        return id == null ? System.identityHashCode(this) : id.hashCode();
    }
}
