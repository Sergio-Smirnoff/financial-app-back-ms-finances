package com.financialapp.finances.service;

import com.financialapp.finances.exception.ResourceNotFoundException;
import com.financialapp.finances.mapper.TransactionMapper;
import com.financialapp.finances.model.dto.request.TransactionRequest;
import com.financialapp.finances.model.dto.response.SummaryResponse;
import com.financialapp.finances.model.dto.response.TransactionResponse;
import com.financialapp.finances.model.entity.Category;
import com.financialapp.finances.model.entity.Transaction;
import com.financialapp.finances.model.enums.TransactionType;
import com.financialapp.finances.repository.CardExpenseInstallmentRepository;
import com.financialapp.finances.repository.CardExpenseRepository;
import com.financialapp.finances.repository.LoanInstallmentRepository;
import com.financialapp.finances.repository.LoanRepository;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private static final List<String> SUPPORTED_CURRENCIES = List.of("ARS", "USD");

    private final TransactionRepository transactionRepository;
    private final LoanRepository loanRepository;
    private final LoanInstallmentRepository loanInstallmentRepository;
    private final CardExpenseRepository cardExpenseRepository;
    private final CardExpenseInstallmentRepository cardExpenseInstallmentRepository;
    private final CategoryService categoryService;
    private final TransactionMapper transactionMapper;

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactions(
            Long userId, TransactionType type, Long categoryId,
            String currency, LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {
        return transactionRepository
                .findFiltered(userId, type, categoryId, currency, dateFrom, dateTo, pageable)
                .map(transactionMapper::toResponse);
    }

    @Transactional
    public TransactionResponse create(Long userId, TransactionRequest request) {
        categoryService.validateSubcategoryForTransaction(request.getCategoryId(), userId);
        Category category = new Category();
        category.setId(request.getCategoryId());
        Transaction transaction = Transaction.builder()
                .userId(userId)
                .type(request.getType())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .category(category)
                .description(request.getDescription())
                .date(request.getDate())
                .build();
        Transaction saved = transactionRepository.save(transaction);
        log.info("Created transaction id={} for userId={}", saved.getId(), userId);
        return transactionMapper.toResponse(transactionRepository.findById(saved.getId())
                .orElseThrow());
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

    private SummaryResponse buildSummary(Long userId, String currency, LocalDate dateFrom, LocalDate dateTo) {
        BigDecimal totalIncome = transactionRepository
                .sumByTypeAndCurrency(userId, TransactionType.INCOME, currency, dateFrom, dateTo);
        BigDecimal transactionExpense = transactionRepository
                .sumByTypeAndCurrency(userId, TransactionType.EXPENSE, currency, dateFrom, dateTo);
        BigDecimal paidLoanInstallments = loanInstallmentRepository
                .sumPaidByUserAndCurrencyAndPaidDateRange(userId, currency, dateFrom, dateTo);
        BigDecimal paidCardInstallments = cardExpenseInstallmentRepository
                .sumPaidByUserAndCurrencyAndPaidDateRange(userId, currency, dateFrom, dateTo);
        BigDecimal totalExpense = transactionExpense.add(paidLoanInstallments).add(paidCardInstallments);
        BigDecimal balance = totalIncome.subtract(totalExpense);
        int activeLoans = loanRepository.countActiveByUserIdAndCurrency(userId, currency);
        BigDecimal totalLoanDebt = loanRepository.sumRemainingDebtByUserIdAndCurrency(userId, currency);
        int activeCardExpenses = cardExpenseRepository.countActiveByUserIdAndCurrency(userId, currency);
        BigDecimal totalCardExpenseDebt = cardExpenseRepository.sumRemainingDebtByUserIdAndCurrency(userId, currency);
        return SummaryResponse.builder()
                .currency(currency)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(balance)
                .activeLoans(activeLoans)
                .totalLoanDebt(totalLoanDebt)
                .activeCardExpenses(activeCardExpenses)
                .totalCardExpenseDebt(totalCardExpenseDebt)
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
