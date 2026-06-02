package com.financialapp.finances.domain.model.category;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.CategoryStatus;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.exception.category.SubcategoryNotInCategoryException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CategoryTest {

    private static final UserId USER = new UserId(1L);
    private static final CategoryName FOOD = new CategoryName("Food");
    private static final CategoryName RESTAURANTS = new CategoryName("Restaurants");

    @Test void createsActiveCategoryWithoutSubcategories() {
        Category c = Category.create(USER, FOOD);
        assertThat(c.id()).isNull();
        assertThat(c.userId()).isEqualTo(USER);
        assertThat(c.name()).isEqualTo(FOOD);
        assertThat(c.status()).isEqualTo(CategoryStatus.ACTIVE);
        assertThat(c.subcategories()).isEmpty();
    }

    @Test void renamesCategory() {
        Category c = Category.create(USER, FOOD).rename(new CategoryName("Groceries"));
        assertThat(c.name()).isEqualTo(new CategoryName("Groceries"));
    }

    @Test void archivesAndRestores() {
        Category archived = Category.create(USER, FOOD).archive();
        assertThat(archived.status()).isEqualTo(CategoryStatus.ARCHIVED);
        assertThat(archived.restore().status()).isEqualTo(CategoryStatus.ACTIVE);
    }

    @Test void addsActiveSubcategory() {
        Category c = Category.create(USER, FOOD).addSubcategory(RESTAURANTS);
        assertThat(c.subcategories()).hasSize(1);
        Subcategory sub = c.subcategories().get(0);
        assertThat(sub.name()).isEqualTo(RESTAURANTS);
        assertThat(sub.status()).isEqualTo(CategoryStatus.ACTIVE);
        assertThat(sub.id()).isNull();
    }

    @Test void archivesSubcategoryById() {
        Category persisted = Category.reconstitute(
            new CategoryId(10L), USER, FOOD, CategoryStatus.ACTIVE,
            List.of(Subcategory.reconstitute(new CategoryId(11L), RESTAURANTS, CategoryStatus.ACTIVE)));
        Category result = persisted.archiveSubcategory(new CategoryId(11L));
        assertThat(result.subcategories().get(0).status()).isEqualTo(CategoryStatus.ARCHIVED);
    }

    @Test void rejectsArchiveOfUnknownSubcategory() {
        Category persisted = Category.reconstitute(
            new CategoryId(10L), USER, FOOD, CategoryStatus.ACTIVE, List.of());
        assertThatThrownBy(() -> persisted.archiveSubcategory(new CategoryId(99L)))
            .isInstanceOf(SubcategoryNotInCategoryException.class);
    }

    @Test void renamesSubcategoryById() {
        Category persisted = Category.reconstitute(
            new CategoryId(10L), USER, FOOD, CategoryStatus.ACTIVE,
            List.of(Subcategory.reconstitute(new CategoryId(11L), RESTAURANTS, CategoryStatus.ACTIVE)));
        Category result = persisted.renameSubcategory(new CategoryId(11L), new CategoryName("Dining"));
        assertThat(result.subcategories().get(0).name()).isEqualTo(new CategoryName("Dining"));
    }

    @Test void restoresArchivedSubcategoryById() {
        Category persisted = Category.reconstitute(
            new CategoryId(10L), USER, FOOD, CategoryStatus.ACTIVE,
            List.of(Subcategory.reconstitute(new CategoryId(11L), RESTAURANTS, CategoryStatus.ARCHIVED)));
        Category result = persisted.restoreSubcategory(new CategoryId(11L));
        assertThat(result.subcategories().get(0).status()).isEqualTo(CategoryStatus.ACTIVE);
    }

    @Test void rejectsRenameOfUnknownSubcategory() {
        Category persisted = Category.reconstitute(
            new CategoryId(10L), USER, FOOD, CategoryStatus.ACTIVE, List.of());
        assertThatThrownBy(() -> persisted.renameSubcategory(new CategoryId(99L), new CategoryName("X")))
            .isInstanceOf(SubcategoryNotInCategoryException.class);
    }

    @Test void rejectsRestoreOfUnknownSubcategory() {
        Category persisted = Category.reconstitute(
            new CategoryId(10L), USER, FOOD, CategoryStatus.ACTIVE, List.of());
        assertThatThrownBy(() -> persisted.restoreSubcategory(new CategoryId(99L)))
            .isInstanceOf(SubcategoryNotInCategoryException.class);
    }
}
