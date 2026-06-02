package com.financialapp.finances.application.category.impl;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.CategoryStatus;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.category.Category;
import com.financialapp.finances.domain.model.category.CategoryName;
import com.financialapp.finances.domain.model.category.Subcategory;
import com.financialapp.finances.domain.repository.CategoryRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CategoryQueryUseCasesTest {

    private final CategoryRepository repo = mock(CategoryRepository.class);
    private final ListCategoriesUseCaseImpl listCategories = new ListCategoriesUseCaseImpl(repo);
    private final GetCategoryUseCaseImpl getCategory = new GetCategoryUseCaseImpl(repo);
    private final ListSubcategoriesUseCaseImpl listSubcategories = new ListSubcategoriesUseCaseImpl(repo);

    private Category active(long id, String name, List<Subcategory> subs) {
        return Category.reconstitute(new CategoryId(id), new UserId(42L),
                new CategoryName(name), CategoryStatus.ACTIVE, subs);
    }

    @Test
    void listReturnsOnlyActiveCategories() {
        Category archived = Category.reconstitute(new CategoryId(2L), new UserId(42L),
                new CategoryName("Old"), CategoryStatus.ARCHIVED, List.of());
        when(repo.findAllOwnedBy(new UserId(42L))).thenReturn(List.of(active(1L, "Food", List.of()), archived));

        List<Category> result = listCategories.execute(new UserId(42L));

        assertThat(result).extracting(c -> c.name().text()).containsExactly("Food");
    }

    @Test
    void getReturnsCategoryOrThrows() {
        when(repo.findByIdOwnedBy(new CategoryId(1L), new UserId(42L)))
                .thenReturn(Optional.of(active(1L, "Food", List.of())));
        assertThat(getCategory.execute(new CategoryId(1L), new UserId(42L)).name().text()).isEqualTo("Food");

        when(repo.findByIdOwnedBy(new CategoryId(9L), new UserId(42L))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> getCategory.execute(new CategoryId(9L), new UserId(42L)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void listSubcategoriesReturnsOnlyActiveOfTheCategory() {
        Subcategory activeSub = Subcategory.reconstitute(new CategoryId(10L), new CategoryName("Groceries"), CategoryStatus.ACTIVE);
        Subcategory archivedSub = Subcategory.reconstitute(new CategoryId(11L), new CategoryName("Old"), CategoryStatus.ARCHIVED);
        when(repo.findByIdOwnedBy(new CategoryId(1L), new UserId(42L)))
                .thenReturn(Optional.of(active(1L, "Food", List.of(activeSub, archivedSub))));

        List<Subcategory> result = listSubcategories.execute(new CategoryId(1L), new UserId(42L));

        assertThat(result).extracting(s -> s.name().text()).containsExactly("Groceries");
    }
}
