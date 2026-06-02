package com.financialapp.finances.domain.exception.category;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.exception.DomainErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SubcategoryNotInCategoryExceptionTest {

    @Test void carriesCodeAndIds_whenIdsPresent() {
        // Given concrete category + sub ids
        SubcategoryNotInCategoryException ex =
                new SubcategoryNotInCategoryException(new CategoryId(1L), new CategoryId(2L));
        // Then the message and details report both, with the stable error code
        assertThat(ex.getError()).isEqualTo(DomainErrorCode.SUBCATEGORY_NOT_IN_CATEGORY);
        assertThat(ex.getMessage()).contains("2").contains("1");
        assertThat(ex.getDetails()).containsEntry("categoryId", "1").containsEntry("subId", "2");
    }

    @Test void rendersNull_whenIdsNull() {
        // Given null ids (defensive branch) / Then the literal "null" is used, no NPE
        SubcategoryNotInCategoryException ex = new SubcategoryNotInCategoryException(null, null);
        assertThat(ex.getMessage()).contains("null");
        assertThat(ex.getDetails()).containsEntry("categoryId", "null").containsEntry("subId", "null");
    }
}
