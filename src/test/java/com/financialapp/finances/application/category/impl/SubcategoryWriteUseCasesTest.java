package com.financialapp.finances.application.category.impl;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.CategoryStatus;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.category.Category;
import com.financialapp.finances.domain.model.category.CategoryName;
import com.financialapp.finances.domain.model.category.Subcategory;
import com.financialapp.finances.domain.repository.CategoryRepository;
import com.financialapp.finances.domain.usecase.category.command.ArchiveSubcategoryCommand;
import com.financialapp.finances.domain.usecase.category.command.CreateSubcategoryCommand;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SubcategoryWriteUseCasesTest {

    private final CategoryRepository repo = mock(CategoryRepository.class);
    private final CreateSubcategoryUseCaseImpl createSub = new CreateSubcategoryUseCaseImpl(repo);
    private final ArchiveSubcategoryUseCaseImpl archiveSub = new ArchiveSubcategoryUseCaseImpl(repo);

    private Category food(List<Subcategory> subs) {
        return Category.reconstitute(new CategoryId(1L), new UserId(42L),
                new CategoryName("Food"), CategoryStatus.ACTIVE, subs);
    }

    @Test
    void createSubcategoryAddsToTheCategory() {
        when(repo.findByIdOwnedBy(new CategoryId(1L), new UserId(42L))).thenReturn(Optional.of(food(List.of())));
        when(repo.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));
        createSub.execute(new CreateSubcategoryCommand(new UserId(42L), new CategoryId(1L), new CategoryName("Groceries")));
        ArgumentCaptor<Category> cap = ArgumentCaptor.forClass(Category.class);
        verify(repo).save(cap.capture());
        assertThat(cap.getValue().subcategories()).extracting(s -> s.name().text()).containsExactly("Groceries");
    }

    @Test
    void archiveSubcategoryFlipsItsStatus() {
        Subcategory sub = Subcategory.reconstitute(new CategoryId(10L), new CategoryName("Groceries"), CategoryStatus.ACTIVE);
        when(repo.findByIdOwnedBy(new CategoryId(1L), new UserId(42L))).thenReturn(Optional.of(food(List.of(sub))));
        when(repo.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));
        archiveSub.execute(new ArchiveSubcategoryCommand(new UserId(42L), new CategoryId(1L), new CategoryId(10L)));
        ArgumentCaptor<Category> cap = ArgumentCaptor.forClass(Category.class);
        verify(repo).save(cap.capture());
        assertThat(cap.getValue().subcategories().get(0).status()).isEqualTo(CategoryStatus.ARCHIVED);
    }
}
