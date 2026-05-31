package com.financialapp.finances.mapper;

import com.financialapp.finances.model.dto.request.CreateParentCategoryRequest;
import com.financialapp.finances.model.dto.response.CategoryFlatResponse;
import com.financialapp.finances.model.dto.response.CategoryTreeResponse;
import com.financialapp.finances.model.dto.response.SubcategoryResponse;
import com.financialapp.finances.model.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "system", source = "system")
    @Mapping(target = "subcategories", ignore = true)
    CategoryTreeResponse toCategoryTreeResponse(Category category);

    @Mapping(target = "system", source = "system")
    SubcategoryResponse toSubcategoryResponse(Category category);

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "system", source = "system")
    CategoryFlatResponse toCategoryFlatResponse(Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "subcategories", ignore = true)
    @Mapping(target = "system", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Category toEntity(CreateParentCategoryRequest request);

    List<SubcategoryResponse> toSubcategoryResponseList(List<Category> categories);
}
