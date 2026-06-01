package com.financialapp.finances.web.mapper;

import com.financialapp.finances.domain.model.category.Category;
import com.financialapp.finances.domain.model.category.Subcategory;
import com.financialapp.finances.web.dto.response.CategoryResponse;
import com.financialapp.finances.web.dto.response.SubcategoryResponse;
import org.springframework.stereotype.Component;

@Component
public class CategoryWebMapper {

    public CategoryResponse toCategoryResponse(Category category) {
        return new CategoryResponse(category.id().value(), category.name().value());
    }

    public SubcategoryResponse toSubcategoryResponse(Subcategory subcategory) {
        return new SubcategoryResponse(subcategory.id().value(), subcategory.name().value());
    }
}
