package com.financialapp.finances.service;

import com.financialapp.finances.client.BanksClient;
import com.financialapp.finances.exception.BusinessException;
import com.financialapp.finances.exception.ResourceNotFoundException;
import com.financialapp.finances.mapper.TransactionMapper;
import com.financialapp.finances.model.dto.request.TransactionRequest;
import com.financialapp.finances.model.dto.request.TransferRequest;
import com.financialapp.finances.model.dto.response.CategorySummaryResponse;
import com.financialapp.finances.model.dto.response.SummaryResponse;
import com.financialapp.finances.model.dto.response.TransactionResponse;
import com.financialapp.finances.model.entity.Category;
import com.financialapp.finances.model.entity.Transaction;
import com.financialapp.finances.model.enums.TransactionType;
import com.financialapp.finances.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private static final List<String> SUPPORTED_CURRENCIES = List.of("ARS", "USD");

    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final TransactionMapper transactionMapper;
    private final BanksClient banksClient;

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactions(
            Long userId, TransactionType type, Long categoryId,
            String currency, LocalDate dateFrom, LocalDate dateTo, 
            List<Long> accountIds, Pageable pageable) {
        return transactionRepository
                .findFiltered(userId, type, categoryId, currency, dateFrom, dateTo, accountIds, pageable)
                .map(transactionMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getByAccount(Long accountId) {
        return transactionRepository.findByAccountIdOrderByDateDesc(accountId).stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @Transactional
    public TransactionResponse create(Long userId, TransactionRequest request) {
        categoryService.validateSubcategoryForTransaction(request.getCategoryId(), userId);
        
        // 1. Adjust balance first to check for funds and currency (fail-fast)
        if (request.getAccountId() != null) {
            BigDecimal delta = request.getType() == TransactionType.INCOME ? 
                    request.getAmount() : request.getAmount().negate();
            banksClient.adjustBalance(request.getAccountId(), delta, request.getCurrency());
        }

        // 2. Save transaction if balance adjust succeeded
        Category category = new Category();
        category.setId(request.getCategoryId());
        Transaction transaction = Transaction.builder()
                .userId(userId)
                .type(request.getType())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .category(category)
                .accountId(request.getAccountId())
                .description(request.getDescription())
                .date(request.getDate())
                .build();
        Transaction saved = transactionRepository.save(transaction);
        log.info("Created transaction id={} for userId={}", saved.getId(), userId);
        
        return transactionMapper.toResponse(saved);
    }

    @Transactional
    public List<TransactionResponse> transfer(Long userId, TransferRequest request) {
        if (request.fromAccountId().equals(request.toAccountId())) {
            throw new BusinessException("Cannot transfer to the same account");
        }

        // 1. Adjust balances (fail-fast on fromAccount funds and currency mismatch)
        // Deduct from source first
        banksClient.adjustBalance(request.fromAccountId(), request.amount().negate(), request.currency());
        // Deposit into target
        banksClient.adjustBalance(request.toAccountId(), request.amount(), request.currency());

        // 2. Record transactions
        UUID transferGroupId = UUID.randomUUID();

        Transaction out = Transaction.builder()
                .userId(userId)
                .accountId(request.fromAccountId())
                .type(TransactionType.EXPENSE)
                .amount(request.amount())
                .currency(request.currency())
                .description(request.description() + " (Transfer Out)")
                .date(request.date())
                .transferGroupId(transferGroupId)
                .build();

        Transaction in = Transaction.builder()
                .userId(userId)
                .accountId(request.toAccountId())
                .type(TransactionType.INCOME)
                .amount(request.amount())
                .currency(request.currency())
                .description(request.description() + " (Transfer In)")
                .date(request.date())
                .transferGroupId(transferGroupId)
                .build();

        Category systemCategory = new Category();
        systemCategory.setId(1101L); // Category: Otros -> Varios
        out.setCategory(systemCategory);
        in.setCategory(systemCategory);

        transactionRepository.save(out);
        transactionRepository.save(in);

        return List.of(transactionMapper.toResponse(out), transactionMapper.toResponse(in));
    }

    @Transactional(readOnly = true)
    public TransactionResponse getById(Long id, Long userId) {
        Transaction transaction = findOwnedTransaction(id, userId);
        return transactionMapper.toResponse(transaction);
    }

    @Transactional
    public TransactionResponse update(Long id, Long userId, TransactionRequest request) {
        Transaction transaction = findOwnedTransaction(id, userId);
        categoryService.validateSubcategoryForTransaction(request.getCategoryId(), userId);
        Category category = new Category();
        category.setId(request.getCategoryId());
        transaction.setType(request.getType());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setCategory(category);
        transaction.setDescription(request.getDescription());
        transaction.setDate(request.getDate());
        return transactionMapper.toResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Transaction transaction = findOwnedTransaction(id, userId);
        transactionRepository.delete(transaction);
        log.info("Deleted transaction id={} for userId={}", id, userId);
    }

    @Transactional(readOnly = true)
    public List<SummaryResponse> getSummary(Long userId, String currency, LocalDate dateFrom, LocalDate dateTo) {
        List<String> currencies = currency != null ? List.of(currency) : SUPPORTED_CURRENCIES;
        return currencies.stream()
                .map(cur -> buildSummary(userId, cur, dateFrom, dateTo))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategorySummaryResponse> getSummaryByCategory(Long userId, LocalDate dateFrom, LocalDate dateTo) {
        return transactionRepository.findSummaryByCategory(userId, dateFrom, dateTo);
    }

    @Transactional
    public void recordPayment(com.financialapp.finances.kafka.event.PaymentEvent event) {
        log.info("Recording payment transaction for userId={} amount={}", event.userId(), event.amount());
        
        // Record only the transaction row. Balance was already adjusted in ms-banks before emitting this event.
        Category systemCategory = new Category();
        systemCategory.setId(1101L); // Category: Otros -> Varios

        // If amount is negative, it's an EXPENSE (money going out).
        // If amount is positive, it's an INCOME (money coming in).
        TransactionType type = event.amount().signum() < 0 ? TransactionType.EXPENSE : TransactionType.INCOME;
        BigDecimal absoluteAmount = event.amount().abs();

        Transaction transaction = Transaction.builder()
                .userId(event.userId())
                .accountId(event.accountId())
                .type(type)
                .amount(absoluteAmount)
                .currency(event.currency())
                .category(systemCategory)
                .description(event.description() != null ? event.description() : "Automatic Payment Recording")
                .date(event.date() != null ? event.date() : LocalDate.now())
                .build();

        transactionRepository.save(transaction);
        log.info("Recorded payment as {} transaction id={}", type, transaction.getId());
    }

    private SummaryResponse buildSummary(Long userId, String currency, LocalDate dateFrom, LocalDate dateTo) {
        BigDecimal totalIncome = transactionRepository
                .sumByTypeAndCurrency(userId, TransactionType.INCOME, currency, dateFrom, dateTo);
        BigDecimal totalExpense = transactionRepository
                .sumByTypeAndCurrency(userId, TransactionType.EXPENSE, currency, dateFrom, dateTo);
        
        BigDecimal balance = totalIncome.subtract(totalExpense);
        
        return SummaryResponse.builder()
                .currency(currency)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(balance)
                .activeLoans(0)
                .totalLoanDebt(BigDecimal.ZERO)
                .activeCardExpenses(0)
                .totalCardExpenseDebt(BigDecimal.ZERO)
                .build();
    }

    private Transaction findOwnedTransaction(Long id, Long userId) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));
        if (!userId.equals(transaction.getUserId())) {
            throw new ResourceNotFoundException("Transaction", id);
        }
        return transaction;
    }
}
