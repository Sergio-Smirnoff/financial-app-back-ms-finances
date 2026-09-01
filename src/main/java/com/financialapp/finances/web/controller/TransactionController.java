package com.financialapp.finances.web.controller;

import com.financialapp.commons.core.domain.model.Cbu;

import com.financialapp.commons.core.domain.model.PageResult;
import com.financialapp.commons.core.response.ApiResponse;
import com.financialapp.commons.web.openapi.ApiErrorCodes;
import com.financialapp.finances.domain.common.model.*;
import com.financialapp.finances.domain.exception.DomainErrorCode;
import com.financialapp.finances.domain.gateway.AccountOwnershipGateway;
import com.financialapp.finances.domain.model.category.CategoryNames;
import com.financialapp.finances.domain.model.transaction.*;
import com.financialapp.finances.domain.repository.CategoryRepository;
import com.financialapp.finances.domain.service.TransactionClassifier;
import com.financialapp.finances.domain.usecase.transaction.*;
import com.financialapp.finances.domain.usecase.transaction.command.DeleteTransactionCommand;
import com.financialapp.finances.domain.usecase.transaction.command.TransactionFilterCommand;
import com.financialapp.finances.web.dto.request.RecordTransactionRequest;
import com.financialapp.finances.web.dto.request.UpdateTransactionRequest;
import com.financialapp.finances.web.dto.response.*;
import com.financialapp.finances.web.mapper.TransactionWebMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "Transaction")
@RestController
@RequestMapping("/api/v1/finances/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final RecordTransaction recordTransaction;
    private final UpdateTransaction updateTransaction;
    private final DeleteTransaction deleteTransaction;
    private final GetTransactionSummary getTransactionSummary;
    private final ListAccountTransactions listAccountTransactions;
    private final ListTransactionsFiltered listTransactionsFiltered;
    private final CountUncategorisedTransactions countUncategorisedTransactions;
    private final GetTransactionDetail getTransactionDetail;
    private final SearchTransactions searchTransactions;
    private final GetMonthlyFlow getMonthlyFlow;
    private final CategoryRepository categoryRepository;
    private final TransactionClassifier classifier;
    private final AccountOwnershipGateway ownershipGateway;
    private final TransactionWebMapper mapper;

    @PostMapping
    @ApiErrorCodes(catalog = DomainErrorCode.class, value = {"invalid_cbu", "invalid_money", "unsupported_currency", "same_account_transaction", "account_currency_mismatch", "transaction_not_owned", "invalid_identifier"})
    public ResponseEntity<ApiResponse<TransactionResponse>> record(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody RecordTransactionRequest req) {
        Transaction saved = recordTransaction.execute(
                mapper.toRecordCommand(new UserId(userId), req));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Transaction recorded", toUser(saved, new UserId(userId))));
    }

    @PutMapping("/{id}")
    @ApiErrorCodes(catalog = DomainErrorCode.class, value = {"invalid_identifier", "invalid_cbu", "invalid_money", "unsupported_currency", "same_account_transaction", "transaction_not_owned"})
    public ResponseEntity<ApiResponse<TransactionResponse>> update(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateTransactionRequest req) {
        Transaction saved = updateTransaction.execute(mapper.toUpdateCommand(new UserId(userId), id, req));
        return ResponseEntity.ok(ApiResponse.ok("Transaction updated", toUser(saved, new UserId(userId))));
    }

    @DeleteMapping("/{id}")
    @ApiErrorCodes(catalog = DomainErrorCode.class, value = {"invalid_identifier", "transaction_not_owned"})
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        deleteTransaction.execute(new DeleteTransactionCommand(new UserId(userId), new TransactionId(id)));
        return ResponseEntity.ok(ApiResponse.ok("Transaction deleted", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> list(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(value = "accountCbu", required = false) String accountCbu,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "kind", required = false) String kindStr,
            @RequestParam(value = "onlyUncategorised", required = false, defaultValue = "false") boolean onlyUncategorised,
            @RequestParam(value = "amountMin", required = false) String amountMinStr,
            @RequestParam(value = "amountMax", required = false) String amountMaxStr,
            @RequestParam(value = "cursor", required = false) String cursor,
            @RequestParam(value = "size", required = false) Integer size) {

        // Legacy ms-banks account-scoped callback (no user context, specific accountCbu and no paging/filter params)
        if (accountCbu != null && cursor == null && size == null && categoryId == null && kindStr == null && !onlyUncategorised && amountMinStr == null && amountMaxStr == null) {
            Cbu cbu = new Cbu(accountCbu);
            List<AccountTransactionResponse> rows = listAccountTransactions.execute(cbu, limit, from, to)
                    .stream().map(v -> mapper.toAccountResponse(v, cbu)).toList();
            return ResponseEntity.ok(ApiResponse.ok(rows));
        }

        if (userId == null) {
            throw new ConstraintViolationException("X-User-Id header is required", Set.of());
        }

        UserId uId = new UserId(userId);
        Cbu cbuParam = accountCbu != null && !accountCbu.isBlank() ? new Cbu(accountCbu) : null;
        CategoryId catIdParam = categoryId != null ? new CategoryId(categoryId) : null;
        DateRange dateRange = (from != null && to != null) ? new DateRange(from, to) : null;
        TransactionKind kind = kindStr != null && !kindStr.isBlank() ? TransactionKind.valueOf(kindStr) : null;
        Money minMoney = amountMinStr != null ? new Money(new BigDecimal(amountMinStr), Currency.getInstance("ARS")) : null;
        Money maxMoney = amountMaxStr != null ? new Money(new BigDecimal(amountMaxStr), Currency.getInstance("ARS")) : null;
        int pageSize = size != null ? size : (limit != null ? limit : 50);

        CursorPage cursorPage = new CursorPage(cursor, pageSize);
        TransactionFilterCommand command = new TransactionFilterCommand(
                uId, cbuParam, catIdParam, dateRange, kind, onlyUncategorised, minMoney, maxMoney, cursorPage);

        PageResult<Transaction> pageResult = listTransactionsFiltered.execute(command);
        Set<Cbu> ownedCbus = ownershipGateway.ownedAccounts(uId);

        List<TransactionResponse> responseList = pageResult.content().stream()
                .map(t -> {
                    TransactionKind k = classifier.classify(t, ownedCbus);
                    CategoryNames names = categoryRepository.findNamesById(t.categoryId()).orElse(new CategoryNames(null, null));
                    String displayName = names.subcategory() != null ? names.subcategory() : names.category();
                    return mapper.toUserResponse(new ClassifiedTransaction(t, k), displayName);
                })
                .toList();

        PageResultResponse<TransactionResponse> pageResponse = new PageResultResponse<>(
                responseList, pageResult.hasNext(), pageResult.nextCursor(), pageResult.totalElements());

        return ResponseEntity.ok(ApiResponse.ok(pageResponse));
    }

    @GetMapping("/uncategorised/count")
    public ResponseEntity<ApiResponse<UncategorisedCountResponse>> countUncategorised(
            @RequestHeader("X-User-Id") Long userId) {
        long count = countUncategorisedTransactions.execute(new UserId(userId));
        return ResponseEntity.ok(ApiResponse.ok(new UncategorisedCountResponse(count)));
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
                        s -> new CurrencySummaryResponse(
                                s.totalIncome().toPlainString(),
                                s.totalExpense().toPlainString(),
                                s.balance().toPlainString()),
                        (a, b) -> a, LinkedHashMap::new));
        return ResponseEntity.ok(ApiResponse.ok(byCurrency));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<TransactionSearchResponse>>> search(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit) {
        UserId user = new UserId(userId);
        List<Transaction> found = searchTransactions.execute(user, q, limit);
        Set<Cbu> ownedCbus = ownershipGateway.ownedAccounts(user);
        List<ClassifiedTransaction> classified = found.stream()
                .map(t -> new ClassifiedTransaction(t, classifier.classify(t, ownedCbus)))
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(mapper.toSearchResponses(classified)));
    }

    @GetMapping("/summary/monthly")
    public ResponseEntity<ApiResponse<List<MonthlyFlowResponse>>> monthlySummary(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<MonthlyFlow> flows = getMonthlyFlow.execute(new UserId(userId), new DateRange(from, to));
        return ResponseEntity.ok(ApiResponse.ok(mapper.toMonthlyFlowResponses(flows)));
    }

    @GetMapping("/{id}")
    @ApiErrorCodes(catalog = DomainErrorCode.class, value = {"transaction_not_found"})
    public ResponseEntity<ApiResponse<TransactionResponse>> detail(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        UserId uId = new UserId(userId);
        Transaction tx = getTransactionDetail.execute(new TransactionId(id), uId);
        TransactionKind kind = classifier.classify(tx, ownershipGateway.ownedAccounts(uId));
        CategoryNames names = categoryRepository.findNamesById(tx.categoryId()).orElse(new CategoryNames(null, null));
        String displayName = names.subcategory() != null ? names.subcategory() : names.category();
        return ResponseEntity.ok(ApiResponse.ok(mapper.toUserResponse(new ClassifiedTransaction(tx, kind), displayName)));
    }

    private TransactionResponse toUser(Transaction saved, UserId userId) {
        TransactionKind kind = classifier.classify(saved, ownershipGateway.ownedAccounts(userId));
        return mapper.toUserResponse(new ClassifiedTransaction(saved, kind), null);
    }
}
