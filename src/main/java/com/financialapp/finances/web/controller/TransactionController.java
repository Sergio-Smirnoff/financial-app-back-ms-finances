package com.financialapp.finances.web.controller;

import com.financialapp.finances.domain.common.model.*;
import com.financialapp.finances.domain.gateway.AccountOwnershipGateway;
import com.financialapp.finances.domain.model.transaction.ClassifiedTransaction;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.model.transaction.TransactionKind;
import com.financialapp.finances.domain.model.transaction.TransactionSummary;
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
import com.financialapp.finances.web.dto.response.TransactionResponse;
import com.financialapp.finances.web.dto.response.TransactionSummaryResponse;
import com.financialapp.finances.web.mapper.TransactionWebMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

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
                new Money(req.amount(), Currency.getInstance(req.currency())),
                new CategoryId(req.categoryId()), req.description(), req.date()));
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
            @RequestParam(value = "from", required = false) LocalDate from,
            @RequestParam(value = "to", required = false) LocalDate to) {
        if (accountCbu != null) {
            Cbu cbu = new Cbu(accountCbu);
            List<AccountTransactionResponse> rows = listAccountTransactions.execute(cbu, limit, from, to)
                    .stream().map(t -> mapper.toAccountResponse(t, cbu)).toList();
            return ResponseEntity.ok(ApiResponse.ok(rows));
        }
        List<TransactionResponse> rows = listUserTransactions.execute(new UserId(userId))
                .stream().map(mapper::toUserResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(rows));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<TransactionSummaryResponse>> summary(
            @RequestHeader("X-User-Id") Long userId) {
        TransactionSummary s = getTransactionSummary.execute(new UserId(userId));
        return ResponseEntity.ok(ApiResponse.ok(new TransactionSummaryResponse(
                s.currency(), s.totalIncome(), s.totalExpense(), s.balance())));
    }

    /** Classify the just-saved aggregate via the domain classifier + ownership gateway (no re-list). */
    private TransactionResponse toUser(Transaction saved, UserId userId) {
        TransactionKind kind = classifier.classify(saved, ownershipGateway.ownedAccounts(userId));
        return mapper.toUserResponse(new ClassifiedTransaction(saved, kind));
    }
}
