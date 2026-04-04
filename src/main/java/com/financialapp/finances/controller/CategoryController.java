package com.financialapp.finances.controller;

import com.financialapp.finances.model.dto.request.CreateParentCategoryRequest;
import com.financialapp.finances.model.dto.request.CreateSubcategoryRequest;
import com.financialapp.finances.model.dto.request.UpdateCategoryRequest;
import com.financialapp.finances.model.dto.request.UpdateSubcategoryRequest;
import com.financialapp.finances.model.dto.response.ApiResponse;
import com.financialapp.finances.model.dto.response.CategoryFlatResponse;
import com.financialapp.finances.model.dto.response.CategoryTreeResponse;
import com.financialapp.finances.model.dto.response.SubcategoryResponse;
import com.financialapp.finances.model.enums.CategoryType;
import com.financialapp.finances.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finances")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management endpoints")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/categories")
    @Operation(summary = "Get category tree", description = "Returns parent categories with nested subcategories")
    public ResponseEntity<ApiResponse<List<CategoryTreeResponse>>> getCategoryTree(
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Filter by type: INCOME, EXPENSE or BOTH")
            @RequestParam(required = false) CategoryType type,
            @Parameter(description = "Filter by system status")
            @RequestParam(required = false) Boolean isSystem) {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getCategoryTree(userId, type, isSystem)));
    }

    @GetMapping("/categories/flat")
    @Operation(summary = "Get flat category list", description = "Returns all categories as a flat list with parentId")
    public ResponseEntity<ApiResponse<List<CategoryFlatResponse>>> getCategoriesFlat(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) CategoryType type,
            @RequestParam(required = false) Boolean isSystem) {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getCategoriesFlat(userId, type, isSystem)));
    }

    @GetMapping("/categories/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<ApiResponse<CategoryFlatResponse>> getById(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getById(id, userId)));
    }

    @GetMapping("/categories/{id}/subcategories")
    @Operation(summary = "Get subcategories of a parent category")
    public ResponseEntity<ApiResponse<List<SubcategoryResponse>>> getSubcategories(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getSubcategories(id, userId)));
    }

    @PostMapping("/categories")
    @Operation(summary = "Create a user parent category")
    public ResponseEntity<ApiResponse<CategoryFlatResponse>> createParentCategory(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateParentCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Category created", categoryService.createParentCategory(request, userId)));
    }

    @PostMapping("/categories/{id}/subcategories")
    @Operation(summary = "Create a subcategory under a parent category")
    public ResponseEntity<ApiResponse<SubcategoryResponse>> createSubcategory(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody CreateSubcategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Subcategory created", categoryService.createSubcategory(id, request, userId)));
    }

    @PutMapping("/categories/{id}")
    @Operation(summary = "Update a user parent category")
    public ResponseEntity<ApiResponse<CategoryFlatResponse>> updateCategory(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.updateParentCategory(id, request, userId)));
    }

    @PutMapping("/subcategories/{id}")
    @Operation(summary = "Update a subcategory (system or user)")
    public ResponseEntity<ApiResponse<SubcategoryResponse>> updateSubcategory(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateSubcategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.updateSubcategory(id, request, userId)));
    }

    @DeleteMapping("/categories/{id}")
    @Operation(summary = "Delete a user parent category (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        categoryService.deleteParentCategory(id, userId);
        return ResponseEntity.ok(ApiResponse.ok("Category deleted", null));
    }

    @DeleteMapping("/subcategories/{id}")
    @Operation(summary = "Delete a user subcategory (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteSubcategory(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        categoryService.deleteSubcategory(id, userId);
        return ResponseEntity.ok(ApiResponse.ok("Subcategory deleted", null));
    }
}
