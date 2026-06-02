package com.financialapp.finances.web.controller;

import com.financialapp.finances.domain.common.model.*;
import com.financialapp.finances.domain.gateway.AccountOwnershipGateway;
import com.financialapp.finances.domain.model.transaction.ClassifiedTransaction;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.model.transaction.TransactionKind;
import com.financialapp.finances.domain.service.TransactionClassifier;
import com.financialapp.finances.domain.usecase.transaction.DeleteTransaction;
import com.financialapp.finances.domain.usecase.transaction.GetTransactionSummary;
import com.financialapp.finances.domain.usecase.transaction.ListAccountTransactions;
import com.financialapp.finances.domain.usecase.transaction.ListUserTransactions;
import com.financialapp.finances.domain.usecase.transaction.RecordTransaction;
import com.financialapp.finances.domain.usecase.transaction.UpdateTransaction;
import com.financialapp.finances.domain.usecase.transaction.command.DeleteTransactionCommand;
import com.financialapp.finances.domain.usecase.transaction.command.RecordTransactionCommand;
import com.financialapp.finances.domain.usecase.transaction.command.UpdateTransactionCommand;
import com.financialapp.finances.web.dto.request.RecordTransactionRequest;
import com.financialapp.finances.web.dto.request.UpdateTransactionRequest;
import com.financialapp.finances.web.dto.response.AccountTransactionResponse;
import com.financialapp.finances.web.dto.response.ApiResponse;
import com.financialapp.finances.web.dto.response.CurrencySummaryResponse;
import com.financialapp.finances.web.dto.response.TransactionResponse;
import com.financialapp.finances.web.mapper.TransactionWebMapper;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Tag(name = "Transaction")
@RestController
@RequestMapping("/api/v1/finances/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final RecordTransaction recordTransaction;
    private final UpdateTransaction updateTransaction;
    private final DeleteTransaction deleteTransaction;
    private final ListUserTransactions listUserTransactions;
    private final GetTransactionSummary getTransactionSummary;
    private final ListAccountTransactions listAccountTransactions;
    private final TransactionClassifier classifier;
    private final AccountOwnershipGateway ownershipGateway;
    private final TransactionWebMapper mapper;

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> record(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody RecordTransactionRequest req) {
        Transaction saved = recordTransaction.execute(new RecordTransactionCommand(
                new UserId(userId), new Cbu(req.fromCbu()), new Cbu(req.toCbu()),
                new Money(req.amount(), Currency.getInstance(req.currency())),
                new CategoryId(req.categoryId()), req.description(), req.date()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Transaction recorded", toUser(saved, new UserId(userId))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> update(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateTransactionRequest req) {
        Transaction saved = updateTransaction.execute(new UpdateTransactionCommand(
                new UserId(userId), new TransactionId(id),
                req.categoryId() != null ? new CategoryId(req.categoryId()) : null,
                req.description(), req.date()));
        return ResponseEntity.ok(ApiResponse.ok("Transaction updated", toUser(saved, new UserId(userId))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        deleteTransaction.execute(new DeleteTransactionCommand(new UserId(userId), new TransactionId(id)));
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true).message("Transaction deleted").build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> list(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(value = "accountCbu", required = false) String accountCbu,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        // Account-scoped listing is the internal ms-banks callback (no user context).
        if (accountCbu != null) {
            Cbu cbu = new Cbu(accountCbu);
            List<AccountTransactionResponse> rows = listAccountTransactions.execute(cbu, limit, from, to)
                    .stream().map(v -> mapper.toAccountResponse(v, cbu)).toList();
            return ResponseEntity.ok(ApiResponse.ok(rows));
        }
        // User-scoped listing requires the gateway-injected X-User-Id header.
        if (userId == null) {
            throw new ConstraintViolationException("X-User-Id header is required", Set.of());
        }
        List<TransactionResponse> rows = listUserTransactions.execute(new UserId(userId))
                .stream().map(mapper::toUserResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(rows));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, CurrencySummaryResponse>>> summary(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if ((from == null) != (to == null)) {
            throw new ConstraintViolationException("from and to must be provided together", Set.of());
        }
        UserId user = new UserId(userId);
        var summaries = (from != null)
                ? getTransactionSummary.execute(user, new DateRange(from, to))
                : getTransactionSummary.execute(user);
        Map<String, CurrencySummaryResponse> byCurrency = summaries.stream()
                .collect(Collectors.toMap(
                        s -> s.currency().getCurrencyCode(),
                        s -> new CurrencySummaryResponse(s.totalIncome(), s.totalExpense(), s.balance()),
                        (a, b) -> a, LinkedHashMap::new));
        return ResponseEntity.ok(ApiResponse.ok(byCurrency));
    }

    /** Classify the just-saved aggregate via the domain classifier + ownership gateway (no re-list). */
    private TransactionResponse toUser(Transaction saved, UserId userId) {
        TransactionKind kind = classifier.classify(saved, ownershipGateway.ownedAccounts(userId));
        return mapper.toUserResponse(new ClassifiedTransaction(saved, kind));
    }
}
