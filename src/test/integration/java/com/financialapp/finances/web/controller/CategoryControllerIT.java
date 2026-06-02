package com.financialapp.finances.web.controller;

import com.financialapp.finances.support.WireMockIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full-stack integration tests for {@link CategoryController}. Every endpoint is driven with a real
 * payload and the response asserted, running the real use cases against the in-memory H2 schema.
 * Categories have no downstream dependency, but the test still boots the full context on the shared
 * WireMock base so the suite follows one integration-test strategy.
 */
class CategoryControllerIT extends WireMockIntegrationTest {

    private static final String USER = "42";

    private long createCategory(String name) throws Exception {
        MvcResult res = mvc.perform(post("/api/v1/finances/categories")
                        .header("X-User-Id", USER).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(res.getResponse().getContentAsString(), "$.data.id")).longValue();
    }

    private long createSubcategory(long categoryId, String name) throws Exception {
        MvcResult res = mvc.perform(post("/api/v1/finances/categories/" + categoryId + "/subcategories")
                        .header("X-User-Id", USER).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(res.getResponse().getContentAsString(), "$.data.id")).longValue();
    }

    @Test
    void create_returnsCreatedCategory() throws Exception {
        mvc.perform(post("/api/v1/finances/categories")
                        .header("X-User-Id", USER).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Food\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Category created"))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.name").value("Food"));
    }

    @Test
    void create_blankName_returns400() throws Exception {
        mvc.perform(post("/api/v1/finances/categories")
                        .header("X-User-Id", USER).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void list_returnsUsersCategories() throws Exception {
        createCategory("Travel");

        mvc.perform(get("/api/v1/finances/categories").header("X-User-Id", USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].name", hasItem("Travel")));
    }

    @Test
    void get_existingCategory_returnsIt() throws Exception {
        long id = createCategory("Utilities");

        mvc.perform(get("/api/v1/finances/categories/" + id).header("X-User-Id", USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value((int) id))
                .andExpect(jsonPath("$.data.name").value("Utilities"));
    }

    @Test
    void get_unknownCategory_returns404() throws Exception {
        mvc.perform(get("/api/v1/finances/categories/999999").header("X-User-Id", USER))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void update_renamesCategory_returns200() throws Exception {
        long id = createCategory("Groserys");

        mvc.perform(put("/api/v1/finances/categories/" + id)
                        .header("X-User-Id", USER).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Groceries\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Category updated"))
                .andExpect(jsonPath("$.data.name").value("Groceries"));
    }

    @Test
    void archive_thenRestore_category() throws Exception {
        long id = createCategory("Hobbies");

        mvc.perform(delete("/api/v1/finances/categories/" + id).header("X-User-Id", USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Category archived"));

        mvc.perform(post("/api/v1/finances/categories/" + id + "/restore").header("X-User-Id", USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Category restored"))
                .andExpect(jsonPath("$.data.name").value("Hobbies"));
    }

    @Test
    void createSubcategory_returnsCreated() throws Exception {
        long parent = createCategory("Housing");

        mvc.perform(post("/api/v1/finances/categories/" + parent + "/subcategories")
                        .header("X-User-Id", USER).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Rent\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Subcategory created"))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.name").value("Rent"));
    }

    @Test
    void createSubcategory_blankName_returns400() throws Exception {
        long parent = createCategory("Transport");

        mvc.perform(post("/api/v1/finances/categories/" + parent + "/subcategories")
                        .header("X-User-Id", USER).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listSubcategories_returnsThem() throws Exception {
        long parent = createCategory("Health");
        createSubcategory(parent, "Pharmacy");

        mvc.perform(get("/api/v1/finances/categories/" + parent + "/subcategories").header("X-User-Id", USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].name", hasItem("Pharmacy")));
    }

    @Test
    void renameSubcategory_returns200() throws Exception {
        long parent = createCategory("Bills");
        long subId = createSubcategory(parent, "Electricty");

        mvc.perform(put("/api/v1/finances/categories/" + parent + "/subcategories/" + subId)
                        .header("X-User-Id", USER).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Electricity\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Subcategory renamed"))
                .andExpect(jsonPath("$.data.id").value((int) subId))
                .andExpect(jsonPath("$.data.name").value("Electricity"));
    }

    @Test
    void renameSubcategory_blankName_returns400() throws Exception {
        long parent = createCategory("Subscriptions");
        long subId = createSubcategory(parent, "Streaming");

        mvc.perform(put("/api/v1/finances/categories/" + parent + "/subcategories/" + subId)
                        .header("X-User-Id", USER).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void archiveSubcategory_thenRestore() throws Exception {
        long parent = createCategory("Food2");
        long subId = createSubcategory(parent, "Restaurants");

        mvc.perform(delete("/api/v1/finances/categories/" + parent + "/subcategories/" + subId).header("X-User-Id", USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Subcategory archived"));

        mvc.perform(post("/api/v1/finances/categories/" + parent + "/subcategories/" + subId + "/restore").header("X-User-Id", USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Subcategory restored"))
                .andExpect(jsonPath("$.data.id").value((int) subId))
                .andExpect(jsonPath("$.data.name").value("Restaurants"));
    }
}
