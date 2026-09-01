package com.financialapp.finances.web.controller;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.RuleId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.rule.CategorizationRule;
import com.financialapp.finances.domain.model.rule.RuleMatchType;
import com.financialapp.finances.domain.usecase.rule.*;
import com.financialapp.finances.web.mapper.CategorizationRuleWebMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CategorizationRuleController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(CategorizationRuleWebMapper.class)
class CategorizationRuleControllerTest {

    @Autowired MockMvc mvc;
    @MockBean CreateCategorizationRule createCategorizationRule;
    @MockBean ListCategorizationRules listCategorizationRules;
    @MockBean DeleteCategorizationRule deleteCategorizationRule;
    @MockBean PreviewCategorizationRule previewCategorizationRule;
    @MockBean SuggestCategories suggestCategories;

    @Test
    void createReturns201() throws Exception {
        CategorizationRule rule = CategorizationRule.reconstitute(
                new RuleId(1L), new UserId(42L), RuleMatchType.CONTAINS, "YPF", new CategoryId(10L), 0, LocalDateTime.now());
        when(createCategorizationRule.execute(any())).thenReturn(new RuleView(rule, "Fuel"));

        mvc.perform(post("/api/v1/finances/categorization-rules")
                        .header("X-User-Id", "42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"matchType\":\"CONTAINS\",\"pattern\":\"YPF\",\"categoryId\":10}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.pattern").value("YPF"))
                .andExpect(jsonPath("$.data.categoryName").value("Fuel"));
    }

    @Test
    void listReturnsRules() throws Exception {
        CategorizationRule rule = CategorizationRule.reconstitute(
                new RuleId(1L), new UserId(42L), RuleMatchType.CONTAINS, "YPF", new CategoryId(10L), 0, LocalDateTime.now());
        when(listCategorizationRules.execute(any())).thenReturn(List.of(new RuleView(rule, "Fuel")));

        mvc.perform(get("/api/v1/finances/categorization-rules")
                        .header("X-User-Id", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].pattern").value("YPF"));
    }

    @Test
    void previewReturnsMatchCountAndSampleIds() throws Exception {
        when(previewCategorizationRule.execute(new RuleId(1L), new UserId(42L)))
                .thenReturn(new RulePreviewResult(5, List.of(100L, 101L)));

        mvc.perform(post("/api/v1/finances/categorization-rules/1/preview")
                        .header("X-User-Id", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.wouldMatchCount").value(5))
                .andExpect(jsonPath("$.data.sampleTransactionIds[0]").value(100))
                .andExpect(jsonPath("$.data.sampleTransactionIds[1]").value(101));
    }

    @Test
    void deleteReturns200() throws Exception {
        mvc.perform(delete("/api/v1/finances/categorization-rules/1")
                        .header("X-User-Id", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        verify(deleteCategorizationRule).execute(new RuleId(1L), new UserId(42L));
    }

    @Test
    void suggestReturnsCategorySuggestions() throws Exception {
        when(suggestCategories.execute(any(), any()))
                .thenReturn(List.of(new CategorySuggestion("YPF RUTA 9", new CategoryId(10L))));

        mvc.perform(post("/api/v1/finances/categorization-rules/suggest")
                        .header("X-User-Id", "42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descriptions\":[\"YPF RUTA 9\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].description").value("YPF RUTA 9"))
                .andExpect(jsonPath("$.data[0].categoryId").value(10));
    }
}
