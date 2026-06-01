package com.financialapp.finances.web.controller;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.CategoryStatus;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.category.Category;
import com.financialapp.finances.domain.model.category.CategoryName;
import com.financialapp.finances.domain.usecase.category.*;
import com.financialapp.finances.web.mapper.CategoryWebMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(CategoryWebMapper.class)
class CategoryControllerTest {

    @Autowired MockMvc mvc;
    @MockBean ListCategories listCategories;
    @MockBean GetCategory getCategory;
    @MockBean ListSubcategories listSubcategories;
    @MockBean CreateCategory createCategory;
    @MockBean UpdateCategory updateCategory;
    @MockBean ArchiveCategory archiveCategory;
    @MockBean RestoreCategory restoreCategory;
    @MockBean CreateSubcategory createSubcategory;
    @MockBean ArchiveSubcategory archiveSubcategory;

    private Category food() {
        return Category.reconstitute(new CategoryId(1L), new UserId(42L),
                new CategoryName("Food"), CategoryStatus.ACTIVE, List.of());
    }

    @Test
    void listReturnsCategories() throws Exception {
        when(listCategories.execute(new UserId(42L))).thenReturn(List.of(food()));
        mvc.perform(get("/api/v1/finances/categories").header("X-User-Id", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Food"));
    }

    @Test
    void createReturns201() throws Exception {
        when(createCategory.execute(any())).thenReturn(food());
        mvc.perform(post("/api/v1/finances/categories").header("X-User-Id", "42")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Food\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Food"));
    }

    @Test
    void createWithBlankNameReturns400() throws Exception {
        mvc.perform(post("/api/v1/finances/categories").header("X-User-Id", "42")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteArchivesAndReturnsSuccess() throws Exception {
        mvc.perform(delete("/api/v1/finances/categories/1").header("X-User-Id", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
