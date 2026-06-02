package com.financialapp.finances.web.mapper;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.CategoryStatus;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.category.Category;
import com.financialapp.finances.domain.model.category.CategoryName;
import com.financialapp.finances.domain.model.category.Subcategory;
import com.financialapp.finances.web.dto.response.CategoryResponse;
import com.financialapp.finances.web.dto.response.SubcategoryResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryWebMapperTest {

    private final CategoryWebMapper mapper = new CategoryWebMapper();

    @Test
    void mapsCategoryAndSubcategory() {
        Category c = Category.reconstitute(new CategoryId(1L), new UserId(42L),
                new CategoryName("Food"), CategoryStatus.ACTIVE, List.of());
        CategoryResponse cr = mapper.toCategoryResponse(c);
        assertThat(cr.id()).isEqualTo(1L);
        assertThat(cr.name()).isEqualTo("Food");

        Subcategory s = Subcategory.reconstitute(new CategoryId(10L), new CategoryName("Groceries"), CategoryStatus.ACTIVE);
        SubcategoryResponse sr = mapper.toSubcategoryResponse(s);
        assertThat(sr.id()).isEqualTo(10L);
        assertThat(sr.name()).isEqualTo("Groceries");
    }
}
