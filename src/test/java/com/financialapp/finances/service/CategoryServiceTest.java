package com.financialapp.finances.service;

import com.financialapp.finances.exception.BusinessException;
import com.financialapp.finances.exception.ResourceNotFoundException;
import com.financialapp.finances.mapper.CategoryMapper;
import com.financialapp.finances.model.dto.request.CreateParentCategoryRequest;
import com.financialapp.finances.model.dto.request.CreateSubcategoryRequest;
import com.financialapp.finances.model.dto.request.UpdateCategoryRequest;
import com.financialapp.finances.model.dto.response.CategoryFlatResponse;
import com.financialapp.finances.model.dto.response.SubcategoryResponse;
import com.financialapp.finances.model.entity.Category;
import com.financialapp.finances.model.enums.CategoryType;
import com.financialapp.finances.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService")
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private CategoryMapper categoryMapper;

    @InjectMocks private CategoryService categoryService;

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;

    private Category buildParent(Long id, Long userId, boolean system) {
        return Category.builder()
                .id(id)
                .userId(system ? null : userId)
                .name("Alimentación")
                .type(CategoryType.EXPENSE)
                .system(system)
                .active(true)
                .build();
    }

    private Category buildSubcategory(Long id, Long userId, boolean system, Category parent) {
        return Category.builder()
                .id(id)
                .userId(system ? null : userId)
                .name("Supermercado")
                .type(CategoryType.EXPENSE)
                .parent(parent)
                .system(system)
                .active(true)
                .build();
    }

    @Nested
    @DisplayName("getSubcategories")
    class GetSubcategories {

        @Test
        @DisplayName("throws BusinessException when called on a subcategory (not a parent)")
        void throwsWhenCalledOnSubcategory() {
            Category parent = buildParent(1L, null, true);
            Category sub = buildSubcategory(101L, USER_ID, false, parent);

            when(categoryRepository.findById(101L)).thenReturn(Optional.of(sub));

            assertThatThrownBy(() -> categoryService.getSubcategories(101L, USER_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("not a parent");
        }
    }

    @Nested
    @DisplayName("createParentCategory")
    class CreateParentCategory {

        @Test
        @DisplayName("creates category with system=false and active=true")
        void createsUserCategory() {
            CreateParentCategoryRequest request = new CreateParentCategoryRequest();
            request.setName("Mascotas");
            request.setType(CategoryType.EXPENSE);

            Category entity = buildParent(50L, USER_ID, false);
            CategoryFlatResponse resp = CategoryFlatResponse.builder().id(50L).name("Mascotas").build();

            when(categoryMapper.toEntity(request)).thenReturn(entity);
            when(categoryRepository.save(entity)).thenReturn(entity);
            when(categoryMapper.toCategoryFlatResponse(entity)).thenReturn(resp);

            CategoryFlatResponse result = categoryService.createParentCategory(request, USER_ID);

            assertThat(result.getId()).isEqualTo(50L);
            assertThat(entity.isSystem()).isFalse();
            assertThat(entity.isActive()).isTrue();
            assertThat(entity.getUserId()).isEqualTo(USER_ID);
        }
    }

    @Nested
    @DisplayName("createSubcategory")
    class CreateSubcategory {

        @Test
        @DisplayName("creates subcategory under a visible parent")
        void createsSubcategory() {
            Category parent = buildParent(1L, null, true);
            CreateSubcategoryRequest request = new CreateSubcategoryRequest();
            request.setName("Almacén");
            request.setType(CategoryType.EXPENSE);

            Category saved = buildSubcategory(200L, USER_ID, false, parent);
            SubcategoryResponse resp = SubcategoryResponse.builder().id(200L).name("Almacén").build();

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(parent));
            when(categoryRepository.save(any(Category.class))).thenReturn(saved);
            when(categoryMapper.toSubcategoryResponse(saved)).thenReturn(resp);

            SubcategoryResponse result = categoryService.createSubcategory(1L, request, USER_ID);

            assertThat(result.getId()).isEqualTo(200L);
        }

        @Test
        @DisplayName("throws BusinessException when parent is itself a subcategory")
        void throwsWhenNestingTooDeep() {
            Category grandParent = buildParent(1L, null, true);
            Category parent = buildSubcategory(101L, null, true, grandParent);

            when(categoryRepository.findById(101L)).thenReturn(Optional.of(parent));

            assertThatThrownBy(() -> categoryService.createSubcategory(101L, new CreateSubcategoryRequest(), USER_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Cannot create subcategory under another subcategory");
        }
    }

    @Nested
    @DisplayName("updateParentCategory")
    class UpdateParentCategory {

        @Test
        @DisplayName("updates name, type, color, icon")
        void updatesFields() {
            Category category = buildParent(50L, USER_ID, false);
            UpdateCategoryRequest request = new UpdateCategoryRequest();
            request.setName("Mascotas actualizado");
            request.setType(CategoryType.BOTH);
            request.setColor("#AABBCC");
            request.setIcon("paw");

            CategoryFlatResponse resp = CategoryFlatResponse.builder().id(50L).build();
            when(categoryRepository.findById(50L)).thenReturn(Optional.of(category));
            when(categoryRepository.save(category)).thenReturn(category);
            when(categoryMapper.toCategoryFlatResponse(category)).thenReturn(resp);

            categoryService.updateParentCategory(50L, request, USER_ID);

            assertThat(category.getName()).isEqualTo("Mascotas actualizado");
            assertThat(category.getType()).isEqualTo(CategoryType.BOTH);
        }

        @Test
        @DisplayName("throws BusinessException when trying to update a system category")
        void throwsWhenSystemCategory() {
            Category system = buildParent(1L, null, true);
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(system));

            assertThatThrownBy(() -> categoryService.updateParentCategory(1L, new UpdateCategoryRequest(), USER_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("System categories cannot be edited");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when category belongs to another user")
        void throwsWhenWrongUser() {
            Category category = buildParent(50L, OTHER_USER_ID, false);
            when(categoryRepository.findById(50L)).thenReturn(Optional.of(category));

            assertThatThrownBy(() -> categoryService.updateParentCategory(50L, new UpdateCategoryRequest(), USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("throws BusinessException when updating a subcategory via parent endpoint")
        void throwsWhenUpdatingSubcategoryViaParentEndpoint() {
            Category parent = buildParent(1L, USER_ID, false);
            Category sub = buildSubcategory(101L, USER_ID, false, parent);
            when(categoryRepository.findById(101L)).thenReturn(Optional.of(sub));

            assertThatThrownBy(() -> categoryService.updateParentCategory(101L, new UpdateCategoryRequest(), USER_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("subcategory endpoint");
        }
    }

    @Nested
    @DisplayName("deleteParentCategory")
    class DeleteParentCategory {

        @Test
        @DisplayName("soft-deletes category by setting active=false")
        void softDeletesCategory() {
            Category category = buildParent(50L, USER_ID, false);
            when(categoryRepository.findById(50L)).thenReturn(Optional.of(category));

            categoryService.deleteParentCategory(50L, USER_ID);

            assertThat(category.isActive()).isFalse();
            verify(categoryRepository).save(category);
        }

        @Test
        @DisplayName("throws BusinessException when deleting a system category")
        void throwsWhenSystemCategory() {
            Category system = buildParent(1L, null, true);
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(system));

            assertThatThrownBy(() -> categoryService.deleteParentCategory(1L, USER_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("System categories cannot be deleted");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when category belongs to another user")
        void throwsWhenWrongUser() {
            Category category = buildParent(50L, OTHER_USER_ID, false);
            when(categoryRepository.findById(50L)).thenReturn(Optional.of(category));

            assertThatThrownBy(() -> categoryService.deleteParentCategory(50L, USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("validateSubcategoryForTransaction")
    class ValidateSubcategoryForTransaction {

        @Test
        @DisplayName("passes validation for active, visible subcategory")
        void passesForValidSubcategory() {
            Category parent = buildParent(1L, null, true);
            Category sub = buildSubcategory(101L, null, true, parent);

            when(categoryRepository.findById(101L)).thenReturn(Optional.of(sub));

            // should not throw
            categoryService.validateSubcategoryForTransaction(101L, USER_ID);
        }

        @Test
        @DisplayName("throws BusinessException when category is a parent (no parent)")
        void throwsWhenParentCategory() {
            Category parent = buildParent(1L, null, true);
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(parent));

            assertThatThrownBy(() -> categoryService.validateSubcategoryForTransaction(1L, USER_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("subcategory");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException for inactive subcategory")
        void throwsForInactiveSubcategory() {
            Category parent = buildParent(1L, null, true);
            Category sub = buildSubcategory(101L, null, true, parent);
            sub.setActive(false);

            when(categoryRepository.findById(101L)).thenReturn(Optional.of(sub));

            assertThatThrownBy(() -> categoryService.validateSubcategoryForTransaction(101L, USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException for user subcategory not owned by requesting user")
        void throwsForOtherUsersSubcategory() {
            Category parent = buildParent(1L, null, true);
            Category sub = buildSubcategory(200L, OTHER_USER_ID, false, parent);

            when(categoryRepository.findById(200L)).thenReturn(Optional.of(sub));

            assertThatThrownBy(() -> categoryService.validateSubcategoryForTransaction(200L, USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
