package com.financialapp.finances.web.controller;

import com.financialapp.commons.core.response.ApiResponse;
import com.financialapp.commons.web.openapi.ApiErrorCodes;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.exception.DomainErrorCode;
import com.financialapp.finances.domain.model.budget.BudgetPeriod;
import com.financialapp.finances.domain.usecase.budget.GetBudgetPace;
import com.financialapp.finances.domain.usecase.budget.GetBudgets;
import com.financialapp.finances.domain.usecase.budget.UpsertBudget;
import com.financialapp.finances.web.dto.request.UpsertBudgetRequest;
import com.financialapp.finances.web.dto.response.BudgetPaceResponse;
import com.financialapp.finances.web.dto.response.BudgetResponse;
import com.financialapp.finances.web.mapper.BudgetWebMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Budget")
@RestController
@RequestMapping("/api/v1/finances/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final UpsertBudget upsertBudget;
    private final GetBudgets getBudgets;
    private final GetBudgetPace getBudgetPace;
    private final BudgetWebMapper mapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> list(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam int year,
            @RequestParam int month) {
        List<BudgetResponse> rows = getBudgets.execute(new UserId(userId), new BudgetPeriod(year, month))
                .stream().map(mapper::toBudgetResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(rows));
    }

    @PutMapping("/{categoryId}")
    @ApiErrorCodes(catalog = DomainErrorCode.class, value = {"invalid_identifier", "invalid_budget"})
    public ResponseEntity<ApiResponse<BudgetResponse>> upsert(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long categoryId,
            @Valid @RequestBody UpsertBudgetRequest req) {
        BudgetResponse response = mapper.toBudgetResponse(
                upsertBudget.execute(mapper.toUpsertCommand(userId, categoryId, req)));
        return ResponseEntity.ok(ApiResponse.ok("Budget upserted", response));
    }

    @GetMapping("/pace")
    public ResponseEntity<ApiResponse<List<BudgetPaceResponse>>> pace(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam int year,
            @RequestParam int month) {
        List<BudgetPaceResponse> rows = getBudgetPace.execute(new UserId(userId), new BudgetPeriod(year, month), LocalDate.now())
                .stream().map(mapper::toBudgetPaceResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(rows));
    }
}
