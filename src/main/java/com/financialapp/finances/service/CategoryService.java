package com.financialapp.finances.service;

import com.financialapp.finances.exception.BusinessException;
import com.financialapp.finances.exception.ResourceNotFoundException;
import com.financialapp.finances.mapper.CategoryMapper;
import com.financialapp.finances.model.dto.request.CreateParentCategoryRequest;
import com.financialapp.finances.model.dto.request.CreateSubcategoryRequest;
import com.financialapp.finances.model.dto.request.UpdateCategoryRequest;
import com.financialapp.finances.model.dto.request.UpdateSubcategoryRequest;
import com.financialapp.finances.model.dto.response.CategoryFlatResponse;
import com.financialapp.finances.model.dto.response.CategoryTreeResponse;
import com.financialapp.finances.model.dto.response.SubcategoryResponse;
import com.financialapp.finances.model.entity.Category;
import com.financialapp.finances.model.enums.CategoryType;
import com.financialapp.finances.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    public List<CategoryTreeResponse> getCategoryTree(Long userId, CategoryType type, Boolean isSystem) {
        List<Category> parents = categoryRepository.findVisibleParentCategories(userId, type, isSystem);
        return parents.stream()
                .map(parent -> {
                    CategoryTreeResponse response = categoryMapper.toCategoryTreeResponse(parent);
                    List<Category> subs = categoryRepository.findVisibleSubcategories(parent.getId(), userId);
                    List<SubcategoryResponse> subResponses = categoryMapper.toSubcategoryResponseList(subs);
                    return CategoryTreeResponse.builder()
                            .id(response.getId())
                            .name(response.getName())
                            .type(response.getType())
                            .color(response.getColor())
                            .icon(response.getIcon())
                            .system(response.getSystem())
                            .subcategories(subResponses)
                            .build();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryFlatResponse> getCategoriesFlat(Long userId, CategoryType type, Boolean isSystem) {
        return categoryRepository.findAllVisibleFlat(userId, type, isSystem)
                .stream()
                .map(categoryMapper::toCategoryFlatResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryFlatResponse getById(Long id, Long userId) {
        Category category = findVisibleCategory(id, userId);
        return categoryMapper.toCategoryFlatResponse(category);
    }

    @Transactional(readOnly = true)
    public List<SubcategoryResponse> getSubcategories(Long parentId, Long userId) {
        Category parent = findVisibleCategory(parentId, userId);
        if (parent.getParent() != null) {
            throw new BusinessException("The specified category is not a parent category");
        }
        return categoryRepository.findVisibleSubcategories(parentId, userId)
                .stream()
                .map(categoryMapper::toSubcategoryResponse)
                .toList();
    }

    @Transactional
    public CategoryFlatResponse createParentCategory(CreateParentCategoryRequest request, Long userId) {
        Category category = categoryMapper.toEntity(request);
        category.setUserId(userId);
        category.setSystem(false);
        category.setActive(true);
        Category saved = categoryRepository.save(category);
        log.info("Created parent category id={} for userId={}", saved.getId(), userId);
        return categoryMapper.toCategoryFlatResponse(saved);
    }

    @Transactional
    public SubcategoryResponse createSubcategory(Long parentId, CreateSubcategoryRequest request, Long userId) {
        Category parent = findVisibleCategory(parentId, userId);
        if (parent.getParent() != null) {
            throw new BusinessException("Cannot create subcategory under another subcategory");
        }
        Category subcategory = Category.builder()
                .parent(parent)
                .name(request.getName())
                .type(request.getType())
                .userId(userId)
                .system(false)
                .active(true)
                .build();
        Category saved = categoryRepository.save(subcategory);
        log.info("Created subcategory id={} under parentId={} for userId={}", saved.getId(), parentId, userId);
        return categoryMapper.toSubcategoryResponse(saved);
    }

    @Transactional
    public CategoryFlatResponse updateParentCategory(Long id, UpdateCategoryRequest request, Long userId) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        if (category.isSystem()) {
            throw new BusinessException("System categories cannot be edited");
        }
        if (!userId.equals(category.getUserId())) {
            throw new ResourceNotFoundException("Category", id);
        }
        if (category.getParent() != null) {
            throw new BusinessException("Use the subcategory endpoint to update subcategories");
        }
        category.setName(request.getName());
        category.setType(request.getType());
        category.setColor(request.getColor());
        category.setIcon(request.getIcon());
        return categoryMapper.toCategoryFlatResponse(categoryRepository.save(category));
    }

    @Transactional
    public SubcategoryResponse updateSubcategory(Long id, UpdateSubcategoryRequest request, Long userId) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        if (category.getParent() == null) {
            throw new BusinessException("Use the category endpoint to update parent categories");
        }
        // System subcategories can be edited but user must be the owner OR it must be a system subcategory
        // visible to this user (system ones are visible to all, user ones only to owner)
        boolean isVisible = category.isSystem() || userId.equals(category.getUserId());
        if (!isVisible) {
            throw new ResourceNotFoundException("Category", id);
        }
        category.setName(request.getName());
        return categoryMapper.toSubcategoryResponse(categoryRepository.save(category));
    }

    @Transactional
    public void deleteParentCategory(Long id, Long userId) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        if (category.isSystem()) {
            throw new BusinessException("System categories cannot be deleted");
        }
        if (!userId.equals(category.getUserId())) {
            throw new ResourceNotFoundException("Category", id);
        }
        if (category.getParent() != null) {
            throw new BusinessException("Use the subcategory endpoint to delete subcategories");
        }
        category.setActive(false);
        categoryRepository.save(category);
        log.info("Deleted (soft) parent category id={} for userId={}", id, userId);
    }

    @Transactional
    public void deleteSubcategory(Long id, Long userId) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        if (category.isSystem()) {
            throw new BusinessException("System subcategories cannot be deleted");
        }
        if (!userId.equals(category.getUserId())) {
            throw new ResourceNotFoundException("Category", id);
        }
        if (category.getParent() == null) {
            throw new BusinessException("Use the category endpoint to delete parent categories");
        }
        category.setActive(false);
        categoryRepository.save(category);
        log.info("Deleted (soft) subcategory id={} for userId={}", id, userId);
    }

    // Validates that the category is a subcategory (has a parent) and is visible to the user
    public void validateSubcategoryForTransaction(Long categoryId, Long userId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
        if (category.getParent() == null) {
            throw new BusinessException("Transactions must be assigned to a subcategory, not a parent category");
        }
        boolean isVisible = category.isSystem() || userId.equals(category.getUserId());
        if (!isVisible || !category.isActive()) {
            throw new ResourceNotFoundException("Category", categoryId);
        }
    }

    private Category findVisibleCategory(Long id, Long userId) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        boolean isVisible = category.isSystem() || userId.equals(category.getUserId());
        if (!isVisible || !category.isActive()) {
            throw new ResourceNotFoundException("Category", id);
        }
        return category;
    }
}
