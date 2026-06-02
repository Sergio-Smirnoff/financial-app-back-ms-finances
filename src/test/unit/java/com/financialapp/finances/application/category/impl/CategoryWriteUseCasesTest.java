package com.financialapp.finances.application.category.impl;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.CategoryStatus;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.category.Category;
import com.financialapp.finances.domain.model.category.CategoryName;
import com.financialapp.finances.domain.repository.CategoryRepository;
import com.financialapp.finances.domain.usecase.category.command.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CategoryWriteUseCasesTest {

    private final CategoryRepository repo = mock(CategoryRepository.class);
    private final CreateCategoryUseCaseImpl create = new CreateCategoryUseCaseImpl(repo);
    private final UpdateCategoryUseCaseImpl update = new UpdateCategoryUseCaseImpl(repo);
    private final ArchiveCategoryUseCaseImpl archive = new ArchiveCategoryUseCaseImpl(repo);
    private final RestoreCategoryUseCaseImpl restore = new RestoreCategoryUseCaseImpl(repo);

    private Category persisted(CategoryStatus status) {
        return Category.reconstitute(new CategoryId(1L), new UserId(42L),
                new CategoryName("Food"), status, List.of());
    }

    @Test
    void createSavesANewActiveCategory() {
        when(repo.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));
        create.execute(new CreateCategoryCommand(new UserId(42L), new CategoryName("Food")));
        ArgumentCaptor<Category> cap = ArgumentCaptor.forClass(Category.class);
        verify(repo).save(cap.capture());
        assertThat(cap.getValue().name().text()).isEqualTo("Food");
        assertThat(cap.getValue().status()).isEqualTo(CategoryStatus.ACTIVE);
        assertThat(cap.getValue().id()).isNull();
    }

    @Test
    void updateRenamesTheLoadedCategory() {
        when(repo.findByIdOwnedBy(new CategoryId(1L), new UserId(42L))).thenReturn(Optional.of(persisted(CategoryStatus.ACTIVE)));
        when(repo.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));
        update.execute(new UpdateCategoryCommand(new UserId(42L), new CategoryId(1L), new CategoryName("Eating out")));
        ArgumentCaptor<Category> cap = ArgumentCaptor.forClass(Category.class);
        verify(repo).save(cap.capture());
        assertThat(cap.getValue().name().text()).isEqualTo("Eating out");
    }

    @Test
    void archiveAndRestoreFlipStatus() {
        when(repo.findByIdOwnedBy(new CategoryId(1L), new UserId(42L)))
                .thenReturn(Optional.of(persisted(CategoryStatus.ACTIVE)));
        when(repo.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));
        assertThat(archive.execute(new ArchiveCategoryCommand(new UserId(42L), new CategoryId(1L))).status())
                .isEqualTo(CategoryStatus.ARCHIVED);

        when(repo.findByIdOwnedBy(new CategoryId(1L), new UserId(42L)))
                .thenReturn(Optional.of(persisted(CategoryStatus.ARCHIVED)));
        assertThat(restore.execute(new RestoreCategoryCommand(new UserId(42L), new CategoryId(1L))).status())
                .isEqualTo(CategoryStatus.ACTIVE);
    }

    @Test
    void updateMissingThrows() {
        when(repo.findByIdOwnedBy(any(), any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> update.execute(new UpdateCategoryCommand(new UserId(42L), new CategoryId(9L), new CategoryName("x"))))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repo, never()).save(any());
    }
}
