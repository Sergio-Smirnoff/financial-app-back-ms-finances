package com.financialapp.finances.web.controller;

import com.financialapp.commons.core.response.ApiResponse;
import com.financialapp.commons.web.openapi.ApiErrorCodes;
import com.financialapp.finances.domain.common.model.RuleId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.exception.DomainErrorCode;
import com.financialapp.finances.domain.usecase.rule.*;
import com.financialapp.finances.web.dto.request.CreateCategorizationRuleRequest;
import com.financialapp.finances.web.dto.request.SuggestCategoriesRequest;
import com.financialapp.finances.web.dto.response.CategorizationRuleResponse;
import com.financialapp.finances.web.dto.response.CategorySuggestionResponse;
import com.financialapp.finances.web.dto.response.RulePreviewResponse;
import com.financialapp.finances.web.mapper.CategorizationRuleWebMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "CategorizationRule")
@RestController
@RequestMapping("/api/v1/finances/categorization-rules")
@RequiredArgsConstructor
public class CategorizationRuleController {

    private final CreateCategorizationRule createCategorizationRule;
    private final ListCategorizationRules listCategorizationRules;
    private final DeleteCategorizationRule deleteCategorizationRule;
    private final PreviewCategorizationRule previewCategorizationRule;
    private final SuggestCategories suggestCategories;
    private final CategorizationRuleWebMapper mapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategorizationRuleResponse>>> list(
            @RequestHeader("X-User-Id") Long userId) {
        List<CategorizationRuleResponse> rows = listCategorizationRules.execute(new UserId(userId))
                .stream().map(mapper::toRuleResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(rows));
    }

    @PostMapping
    @ApiErrorCodes(catalog = DomainErrorCode.class, value = {"invalid_identifier", "invalid_categorization_rule"})
    public ResponseEntity<ApiResponse<CategorizationRuleResponse>> create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateCategorizationRuleRequest req) {
        CategorizationRuleResponse response = mapper.toRuleResponse(
                createCategorizationRule.execute(mapper.toCreateCommand(userId, req)));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Categorization rule created", response));
    }

    @PostMapping("/{id}/preview")
    @ApiErrorCodes(catalog = DomainErrorCode.class, value = {"invalid_identifier", "categorization_rule_not_found"})
    public ResponseEntity<ApiResponse<RulePreviewResponse>> preview(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        RulePreviewResult result = previewCategorizationRule.execute(new RuleId(id), new UserId(userId));
        return ResponseEntity.ok(ApiResponse.ok(mapper.toPreviewResponse(result)));
    }

    @DeleteMapping("/{id}")
    @ApiErrorCodes(catalog = DomainErrorCode.class, value = {"invalid_identifier", "categorization_rule_not_found"})
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        deleteCategorizationRule.execute(new RuleId(id), new UserId(userId));
        return ResponseEntity.ok(ApiResponse.ok("Categorization rule deleted", null));
    }

    @PostMapping("/suggest")
    public ResponseEntity<ApiResponse<List<CategorySuggestionResponse>>> suggest(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody SuggestCategoriesRequest req) {
        List<String> descriptions = req.descriptions() != null ? req.descriptions() : List.of();
        List<CategorySuggestionResponse> rows = suggestCategories.execute(new UserId(userId), descriptions)
                .stream().map(mapper::toSuggestionResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(rows));
    }
}
