package com.financialapp.finances.web.mapper;

import com.financialapp.finances.domain.common.model.CategoryStatus;
import com.financialapp.finances.domain.model.category.Category;
import com.financialapp.finances.domain.model.category.Subcategory;
import com.financialapp.finances.web.dto.response.CategoryResponse;
import com.financialapp.finances.web.dto.response.SubcategoryResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoryWebMapper {

    public CategoryResponse toCategoryResponse(Category category) {
        List<SubcategoryResponse> subcategories = category.subcategories().stream()
                .filter(s -> s.status() == CategoryStatus.ACTIVE)
                .map(this::toSubcategoryResponse)
                .toList();
        return new CategoryResponse(category.id().value(), category.name().text(), subcategories);
    }

    public SubcategoryResponse toSubcategoryResponse(Subcategory subcategory) {
        return new SubcategoryResponse(subcategory.id().value(), subcategory.name().text());
    }
}
