package com.financialapp.finances.domain.model.category;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.CategoryStatus;
import com.financialapp.finances.domain.common.model.UserId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SubcategoryBranchesTest {

    private static final CategoryName NAME = new CategoryName("Dining");

    @Test void equals_trueForSamePersistedId_falseOtherwise() {
        // Given two subcategories sharing id 11
        Subcategory a = Subcategory.reconstitute(new CategoryId(11L), NAME, CategoryStatus.ACTIVE);
        Subcategory b = Subcategory.reconstitute(new CategoryId(11L), new CategoryName("Other"), CategoryStatus.ARCHIVED);
        Subcategory other = Subcategory.reconstitute(new CategoryId(12L), NAME, CategoryStatus.ACTIVE);
        // Then identity is by id
        assertThat(a).isEqualTo(a).isEqualTo(b).isNotEqualTo(other)
                .isNotEqualTo(null).isNotEqualTo("not-a-subcategory");
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test void equals_falseForUnpersistedInstances() {
        // Given two distinct unpersisted subcategories (null id), reached via the aggregate root
        Category c = Category.create(new UserId(1L), new CategoryName("Food"))
                .addSubcategory(NAME).addSubcategory(new CategoryName("Groceries"));
        List<Subcategory> subs = c.subcategories();
        Subcategory first = subs.get(0);
        Subcategory second = subs.get(1);
        assertThat(first).isEqualTo(first).isNotEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(System.identityHashCode(first));
    }
}
