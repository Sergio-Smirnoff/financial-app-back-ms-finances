package com.financialapp.finances.web.controller;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.category.Category;
import com.financialapp.finances.domain.model.category.CategoryName;
import com.financialapp.finances.domain.model.category.Subcategory;
import com.financialapp.finances.domain.usecase.category.*;
import com.financialapp.finances.domain.usecase.category.command.*;
import com.financialapp.finances.web.dto.request.CreateCategoryRequest;
import com.financialapp.finances.web.dto.request.CreateSubcategoryRequest;
import com.financialapp.finances.web.dto.request.RenameSubcategoryRequest;
import com.financialapp.finances.web.dto.request.UpdateCategoryRequest;
import com.financialapp.finances.web.dto.response.ApiResponse;
import com.financialapp.finances.web.dto.response.CategoryResponse;
import com.financialapp.finances.web.dto.response.SubcategoryResponse;
import com.financialapp.finances.web.mapper.CategoryWebMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Category")
@RestController
@RequestMapping("/api/v1/finances/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final ListCategories listCategories;
    private final GetCategory getCategory;
    private final ListSubcategories listSubcategories;
    private final CreateCategory createCategory;
    private final UpdateCategory updateCategory;
    private final ArchiveCategory archiveCategory;
    private final RestoreCategory restoreCategory;
    private final CreateSubcategory createSubcategory;
    private final ArchiveSubcategory archiveSubcategory;
    private final RenameSubcategory renameSubcategory;
    private final RestoreSubcategory restoreSubcategory;
    private final CategoryWebMapper mapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> list(@RequestHeader("X-User-Id") Long userId) {
        List<CategoryResponse> rows = listCategories.execute(new UserId(userId))
                .stream().map(mapper::toCategoryResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(rows));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> get(
            @RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        Category category = getCategory.execute(new CategoryId(id), new UserId(userId));
        return ResponseEntity.ok(ApiResponse.ok(mapper.toCategoryResponse(category)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            @RequestHeader("X-User-Id") Long userId, @Valid @RequestBody CreateCategoryRequest req) {
        Category saved = createCategory.execute(
                new CreateCategoryCommand(new UserId(userId), new CategoryName(req.name())));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Category created", mapper.toCategoryResponse(saved)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            @RequestHeader("X-User-Id") Long userId, @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest req) {
        Category saved = updateCategory.execute(
                new UpdateCategoryCommand(new UserId(userId), new CategoryId(id), new CategoryName(req.name())));
        return ResponseEntity.ok(ApiResponse.ok("Category updated", mapper.toCategoryResponse(saved)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> archive(
            @RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        archiveCategory.execute(new ArchiveCategoryCommand(new UserId(userId), new CategoryId(id)));
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Category archived").build());
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<CategoryResponse>> restore(
            @RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        Category saved = restoreCategory.execute(new RestoreCategoryCommand(new UserId(userId), new CategoryId(id)));
        return ResponseEntity.ok(ApiResponse.ok("Category restored", mapper.toCategoryResponse(saved)));
    }

    @GetMapping("/{id}/subcategories")
    public ResponseEntity<ApiResponse<List<SubcategoryResponse>>> listSubcategoriesOf(
            @RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        List<SubcategoryResponse> rows = listSubcategories.execute(new CategoryId(id), new UserId(userId))
                .stream().map(mapper::toSubcategoryResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(rows));
    }

    @PostMapping("/{id}/subcategories")
    public ResponseEntity<ApiResponse<SubcategoryResponse>> createSubcategoryIn(
            @RequestHeader("X-User-Id") Long userId, @PathVariable Long id,
            @Valid @RequestBody CreateSubcategoryRequest req) {
        Category saved = createSubcategory.execute(
                new CreateSubcategoryCommand(new UserId(userId), new CategoryId(id), new CategoryName(req.name())));
        List<Subcategory> subcategories = saved.subcategories();
        Subcategory created = subcategories.get(subcategories.size() - 1);   // the just-added subcategory
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Subcategory created", mapper.toSubcategoryResponse(created)));
    }

    @DeleteMapping("/{id}/subcategories/{subId}")
    public ResponseEntity<ApiResponse<Void>> archiveSubcategoryIn(
            @RequestHeader("X-User-Id") Long userId, @PathVariable Long id, @PathVariable Long subId) {
        archiveSubcategory.execute(new ArchiveSubcategoryCommand(new UserId(userId), new CategoryId(id), new CategoryId(subId)));
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Subcategory archived").build());
    }

    @PutMapping("/{id}/subcategories/{subId}")
    public ResponseEntity<ApiResponse<SubcategoryResponse>> renameSubcategoryIn(
            @RequestHeader("X-User-Id") Long userId, @PathVariable Long id, @PathVariable Long subId,
            @Valid @RequestBody RenameSubcategoryRequest req) {
        Category saved = renameSubcategory.execute(new RenameSubcategoryCommand(
                new UserId(userId), new CategoryId(id), new CategoryId(subId), new CategoryName(req.name())));
        return ResponseEntity.ok(ApiResponse.ok("Subcategory renamed",
                mapper.toSubcategoryResponse(subcategoryOf(saved, subId))));
    }

    @PostMapping("/{id}/subcategories/{subId}/restore")
    public ResponseEntity<ApiResponse<SubcategoryResponse>> restoreSubcategoryIn(
            @RequestHeader("X-User-Id") Long userId, @PathVariable Long id, @PathVariable Long subId) {
        Category saved = restoreSubcategory.execute(new RestoreSubcategoryCommand(
                new UserId(userId), new CategoryId(id), new CategoryId(subId)));
        return ResponseEntity.ok(ApiResponse.ok("Subcategory restored",
                mapper.toSubcategoryResponse(subcategoryOf(saved, subId))));
    }

    private Subcategory subcategoryOf(Category saved, Long subId) {
        return saved.subcategories().stream()
                .filter(s -> s.id().value().equals(subId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("subcategory " + subId + " missing after save"));
    }
}
