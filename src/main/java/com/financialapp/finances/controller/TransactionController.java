package com.financialapp.finances.controller;

import com.financialapp.finances.model.dto.request.TransactionRequest;
import com.financialapp.finances.model.dto.response.ApiResponse;
import com.financialapp.finances.model.dto.response.SummaryResponse;
import com.financialapp.finances.model.dto.response.TransactionResponse;
import com.financialapp.finances.model.enums.TransactionType;
import com.financialapp.finances.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/finances/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Income and expense transaction management")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    @Operation(summary = "List transactions (paginated)", description = "Supports filters: type, categoryId, currency, dateFrom, dateTo")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactions(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String currency,
            @Parameter(description = "ISO date yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                transactionService.getTransactions(userId, type, categoryId, currency, dateFrom, dateTo, pageable)));
    }

    @PostMapping
    @Operation(summary = "Create a transaction")
    public ResponseEntity<ApiResponse<TransactionResponse>> create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Transaction created", transactionService.create(userId, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID")
    public ResponseEntity<ApiResponse<TransactionResponse>> getById(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(transactionService.getById(id, userId)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a transaction")
    public ResponseEntity<ApiResponse<TransactionResponse>> update(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(transactionService.update(id, userId, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a transaction (physical delete)")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        transactionService.delete(id, userId);
        return ResponseEntity.ok(ApiResponse.ok("Transaction deleted", null));
    }

    @GetMapping("/summary")
    @Operation(summary = "Financial summary per currency",
               description = "Returns income, expense, balance, active loans and card expenses for each currency")
    public ResponseEntity<ApiResponse<List<SummaryResponse>>> getSummary(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return ResponseEntity.ok(ApiResponse.ok(transactionService.getSummary(userId, currency, dateFrom, dateTo)));
    }
}
