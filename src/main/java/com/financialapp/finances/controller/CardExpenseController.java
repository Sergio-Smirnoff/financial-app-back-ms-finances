package com.financialapp.finances.controller;

import com.financialapp.finances.model.dto.request.CardExpenseRequest;
import com.financialapp.finances.model.dto.request.CardExpenseUpdateRequest;
import com.financialapp.finances.model.dto.response.ApiResponse;
import com.financialapp.finances.model.dto.response.CardExpenseInstallmentResponse;
import com.financialapp.finances.model.dto.response.CardExpenseResponse;
import com.financialapp.finances.service.CardExpenseInstallmentService;
import com.financialapp.finances.service.CardExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finances/card-expenses")
@RequiredArgsConstructor
@Tag(name = "Card Expenses", description = "Card installment expense management")
public class CardExpenseController {

    private final CardExpenseService cardExpenseService;
    private final CardExpenseInstallmentService installmentService;

    @GetMapping
    @Operation(summary = "List card expenses", description = "Filters: active, cardId, currency")
    public ResponseEntity<ApiResponse<List<CardExpenseResponse>>> getCardExpenses(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Long cardId,
            @RequestParam(required = false) String currency) {
        return ResponseEntity.ok(ApiResponse.ok(cardExpenseService.getCardExpenses(userId, active, cardId, currency)));
    }

    @PostMapping
    @Operation(summary = "Register a new card expense")
    public ResponseEntity<ApiResponse<CardExpenseResponse>> create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CardExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Card expense created", cardExpenseService.create(userId, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get card expense by ID")
    public ResponseEntity<ApiResponse<CardExpenseResponse>> getById(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(cardExpenseService.getById(id, userId)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update card expense description and card reference")
    public ResponseEntity<ApiResponse<CardExpenseResponse>> update(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody CardExpenseUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(cardExpenseService.update(id, userId, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a card expense")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        cardExpenseService.delete(id, userId);
        return ResponseEntity.ok(ApiResponse.ok("Card expense deleted", null));
    }

    @Deprecated
    @PostMapping("/{id}/pay-installment")
    @Operation(summary = "[Deprecated] Pay the next unpaid installment",
               description = "Deprecated since v1.1 — use POST /{id}/installments/{installmentId}/pay instead. " +
                             "Pays the first unpaid installment automatically.")
    public ResponseEntity<ApiResponse<CardExpenseInstallmentResponse>> payNextInstallment(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Installment paid",
                installmentService.payNextInstallment(id, userId)));
    }

    @GetMapping("/{id}/installments")
    @Operation(summary = "List installments of a card expense")
    public ResponseEntity<ApiResponse<List<CardExpenseInstallmentResponse>>> getInstallments(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(installmentService.getInstallments(id, userId)));
    }

    @PostMapping("/{id}/installments/{installmentId}/pay")
    @Operation(summary = "Pay a specific installment",
               description = "Marks installment as paid, decrements remaining count, updates next due date. Sequential payment required.")
    public ResponseEntity<ApiResponse<CardExpenseInstallmentResponse>> payInstallment(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @PathVariable Long installmentId) {
        return ResponseEntity.ok(ApiResponse.ok("Installment paid",
                installmentService.payInstallment(id, installmentId, userId)));
    }
}
