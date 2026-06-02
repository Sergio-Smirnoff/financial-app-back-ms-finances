package com.financialapp.finances.domain.model.category;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.CategoryStatus;
import com.financialapp.finances.domain.common.model.UserId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryBranchesTest {

    private static final UserId USER = new UserId(1L);
    private static final CategoryName FOOD = new CategoryName("Food");

    private Category withTwoSubs(CategoryStatus s11, CategoryStatus s12) {
        return Category.reconstitute(new CategoryId(10L), USER, FOOD, CategoryStatus.ACTIVE,
                List.of(Subcategory.reconstitute(new CategoryId(11L), new CategoryName("A"), s11),
                        Subcategory.reconstitute(new CategoryId(12L), new CategoryName("B"), s12)));
    }

    @Test void archiveSubcategory_leavesNonMatchingSiblingsUntouched() {
        // Given two active subs / When archiving one / Then only that one flips
        Category result = withTwoSubs(CategoryStatus.ACTIVE, CategoryStatus.ACTIVE)
                .archiveSubcategory(new CategoryId(11L));
        assertThat(result.subcategories().get(0).status()).isEqualTo(CategoryStatus.ARCHIVED);
        assertThat(result.subcategories().get(1).status()).isEqualTo(CategoryStatus.ACTIVE);
    }

    @Test void renameSubcategory_leavesNonMatchingSiblingsUntouched() {
        // Given two subs / When renaming the first / Then the sibling keeps its name
        Category result = withTwoSubs(CategoryStatus.ACTIVE, CategoryStatus.ACTIVE)
                .renameSubcategory(new CategoryId(11L), new CategoryName("Renamed"));
        assertThat(result.subcategories().get(0).name().text()).isEqualTo("Renamed");
        assertThat(result.subcategories().get(1).name().text()).isEqualTo("B");
    }

    @Test void restoreSubcategory_leavesNonMatchingSiblingsUntouched() {
        // Given a sibling archived / When restoring the target / Then the sibling stays archived
        Category result = withTwoSubs(CategoryStatus.ARCHIVED, CategoryStatus.ARCHIVED)
                .restoreSubcategory(new CategoryId(11L));
        assertThat(result.subcategories().get(0).status()).isEqualTo(CategoryStatus.ACTIVE);
        assertThat(result.subcategories().get(1).status()).isEqualTo(CategoryStatus.ARCHIVED);
    }

    @Test void equals_trueForSamePersistedId_falseOtherwise() {
        // Given two reconstituted categories sharing id 10
        Category a = Category.reconstitute(new CategoryId(10L), USER, FOOD, CategoryStatus.ACTIVE, List.of());
        Category b = Category.reconstitute(new CategoryId(10L), USER, new CategoryName("Other"), CategoryStatus.ARCHIVED, List.of());
        Category other = Category.reconstitute(new CategoryId(20L), USER, FOOD, CategoryStatus.ACTIVE, List.of());
        // Then identity is by id
        assertThat(a).isEqualTo(a).isEqualTo(b).isNotEqualTo(other)
                .isNotEqualTo(null).isNotEqualTo("not-a-category");
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test void equals_falseForUnpersistedInstances() {
        // Given two distinct unpersisted categories (null id) / Then they are equal only to themselves
        Category a = Category.create(USER, FOOD);
        Category b = Category.create(USER, FOOD);
        assertThat(a).isEqualTo(a).isNotEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(System.identityHashCode(a));
    }
}
