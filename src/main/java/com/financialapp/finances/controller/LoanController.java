package com.financialapp.finances.controller;

import com.financialapp.finances.model.dto.request.LoanRequest;
import com.financialapp.finances.model.dto.request.LoanUpdateRequest;
import com.financialapp.finances.model.dto.response.ApiResponse;
import com.financialapp.finances.model.dto.response.LoanInstallmentResponse;
import com.financialapp.finances.model.dto.response.LoanResponse;
import com.financialapp.finances.service.LoanInstallmentService;
import com.financialapp.finances.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finances/loans")
@RequiredArgsConstructor
@Tag(name = "Loans", description = "Loan and installment management")
public class LoanController {

    private final LoanService loanService;
    private final LoanInstallmentService loanInstallmentService;

    @GetMapping
    @Operation(summary = "List loans", description = "Filters: active, currency. Supports pagination.")
    public ResponseEntity<ApiResponse<Page<LoanResponse>>> getLoans(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String currency,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(loanService.getLoans(userId, active, currency, pageable)));
    }

    @PostMapping
    @Operation(summary = "Create a loan", description = "Automatically generates all installments")
    public ResponseEntity<ApiResponse<LoanResponse>> create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody LoanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Loan created", loanService.create(userId, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get loan by ID")
    public ResponseEntity<ApiResponse<LoanResponse>> getById(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(loanService.getById(id, userId)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a loan", description = "Only description and entity can be updated")
    public ResponseEntity<ApiResponse<LoanResponse>> update(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody LoanUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(loanService.update(id, userId, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a loan and all its installments")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        loanService.delete(id, userId);
        return ResponseEntity.ok(ApiResponse.ok("Loan deleted", null));
    }

    @GetMapping("/{id}/installments")
    @Operation(summary = "Get all installments of a loan")
    public ResponseEntity<ApiResponse<List<LoanInstallmentResponse>>> getInstallments(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(loanInstallmentService.getInstallments(id, userId)));
    }

    @PutMapping("/{id}/installments/{installmentId}/pay")
    @Operation(summary = "Mark an installment as paid",
               description = "Validates no previous installments are unpaid. Updates loan's paid count and next payment date.")
    public ResponseEntity<ApiResponse<LoanInstallmentResponse>> payInstallment(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @PathVariable Long installmentId) {
        return ResponseEntity.ok(ApiResponse.ok("Installment paid",
                loanInstallmentService.payInstallment(id, installmentId, userId)));
    }
}
