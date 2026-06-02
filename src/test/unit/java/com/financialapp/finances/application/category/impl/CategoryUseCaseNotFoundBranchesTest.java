package com.financialapp.finances.application.category.impl;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.CategoryStatus;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.category.Category;
import com.financialapp.finances.domain.model.category.CategoryName;
import com.financialapp.finances.domain.model.category.Subcategory;
import com.financialapp.finances.domain.repository.CategoryRepository;
import com.financialapp.finances.domain.usecase.category.command.ArchiveCategoryCommand;
import com.financialapp.finances.domain.usecase.category.command.ArchiveSubcategoryCommand;
import com.financialapp.finances.domain.usecase.category.command.CreateSubcategoryCommand;
import com.financialapp.finances.domain.usecase.category.command.RenameSubcategoryCommand;
import com.financialapp.finances.domain.usecase.category.command.RestoreCategoryCommand;
import com.financialapp.finances.domain.usecase.category.command.RestoreSubcategoryCommand;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CategoryUseCaseNotFoundBranchesTest {

    private final CategoryRepository repo = mock(CategoryRepository.class);
    private final UserId user = new UserId(42L);
    private final CategoryId catId = new CategoryId(1L);

    private Category foodWithSub(CategoryStatus subStatus) {
        return Category.reconstitute(catId, user, new CategoryName("Food"), CategoryStatus.ACTIVE,
                List.of(Subcategory.reconstitute(new CategoryId(11L), new CategoryName("Dining"), subStatus)));
    }

    @Test void archiveCategory_throws_whenNotOwned() {
        when(repo.findByIdOwnedBy(any(), any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> new ArchiveCategoryUseCaseImpl(repo)
                .execute(new ArchiveCategoryCommand(user, catId)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repo, never()).save(any());
    }

    @Test void restoreCategory_throws_whenNotOwned() {
        when(repo.findByIdOwnedBy(any(), any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> new RestoreCategoryUseCaseImpl(repo)
                .execute(new RestoreCategoryCommand(user, catId)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repo, never()).save(any());
    }

    @Test void createSubcategory_throws_whenCategoryNotOwned() {
        when(repo.findByIdOwnedBy(any(), any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> new CreateSubcategoryUseCaseImpl(repo)
                .execute(new CreateSubcategoryCommand(user, catId, new CategoryName("New"))))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repo, never()).save(any());
    }

    @Test void archiveSubcategory_throws_whenCategoryNotOwned() {
        when(repo.findByIdOwnedBy(any(), any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> new ArchiveSubcategoryUseCaseImpl(repo)
                .execute(new ArchiveSubcategoryCommand(user, catId, new CategoryId(11L))))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repo, never()).save(any());
    }

    @Test void renameSubcategory_throws_whenCategoryNotOwned() {
        when(repo.findByIdOwnedBy(any(), any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> new RenameSubcategoryUseCaseImpl(repo)
                .execute(new RenameSubcategoryCommand(user, catId, new CategoryId(11L), new CategoryName("X"))))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repo, never()).save(any());
    }

    @Test void restoreSubcategory_throws_whenCategoryNotOwned() {
        when(repo.findByIdOwnedBy(any(), any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> new RestoreSubcategoryUseCaseImpl(repo)
                .execute(new RestoreSubcategoryCommand(user, catId, new CategoryId(11L))))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repo, never()).save(any());
    }

    @Test void listSubcategories_throws_whenCategoryNotOwned() {
        when(repo.findByIdOwnedBy(any(), any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> new ListSubcategoriesUseCaseImpl(repo).execute(catId, user))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test void renameSubcategory_renamesAndSaves_whenOwned() {
        when(repo.findByIdOwnedBy(catId, user)).thenReturn(Optional.of(foodWithSub(CategoryStatus.ACTIVE)));
        when(repo.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));
        new RenameSubcategoryUseCaseImpl(repo)
                .execute(new RenameSubcategoryCommand(user, catId, new CategoryId(11L), new CategoryName("Renamed")));
        ArgumentCaptor<Category> cap = ArgumentCaptor.forClass(Category.class);
        verify(repo).save(cap.capture());
        assertThat(cap.getValue().subcategories().get(0).name().text()).isEqualTo("Renamed");
    }

    @Test void restoreSubcategory_restoresAndSaves_whenOwned() {
        when(repo.findByIdOwnedBy(catId, user)).thenReturn(Optional.of(foodWithSub(CategoryStatus.ARCHIVED)));
        when(repo.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));
        new RestoreSubcategoryUseCaseImpl(repo)
                .execute(new RestoreSubcategoryCommand(user, catId, new CategoryId(11L)));
        ArgumentCaptor<Category> cap = ArgumentCaptor.forClass(Category.class);
        verify(repo).save(cap.capture());
        assertThat(cap.getValue().subcategories().get(0).status()).isEqualTo(CategoryStatus.ACTIVE);
    }
}
