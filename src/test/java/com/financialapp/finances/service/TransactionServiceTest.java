package com.financialapp.finances.service;

import com.financialapp.finances.exception.BusinessException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService")
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private LoanRepository loanRepository;
    @Mock private LoanInstallmentRepository loanInstallmentRepository;
    @Mock private CardExpenseRepository cardExpenseRepository;
    @Mock private CardExpenseInstallmentRepository cardExpenseInstallmentRepository;
    @Mock private CategoryService categoryService;
    @Mock private TransactionMapper transactionMapper;

    @InjectMocks private TransactionService transactionService;

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;

    private Transaction buildTransaction(Long id, Long userId) {
        Category category = new Category();
        category.setId(101L);
        category.setName("Supermercado");
        return Transaction.builder()
                .id(id)
                .userId(userId)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("1500.00"))
                .currency("ARS")
                .category(category)
                .description("Compra semanal")
                .date(LocalDate.of(2025, 1, 15))
                .build();
    }

    private TransactionResponse buildResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .userId(t.getUserId())
                .type(t.getType().name())
                .amount(t.getAmount())
                .currency(t.getCurrency())
                .categoryId(t.getCategory().getId())
                .categoryName(t.getCategory().getName())
                .description(t.getDescription())
                .date(t.getDate())
                .build();
    }

    @Nested
    @DisplayName("getTransactions")
    class GetTransactions {

        @Test
        @DisplayName("returns paginated transactions filtered by userId")
        void returnsPagedTransactions() {
            Transaction tx = buildTransaction(1L, USER_ID);
            TransactionResponse resp = buildResponse(tx);
            Page<Transaction> page = new PageImpl<>(List.of(tx));
            Pageable pageable = PageRequest.of(0, 20);

            when(transactionRepository.findFiltered(USER_ID, null, null, null, null, null, pageable))
                    .thenReturn(page);
            when(transactionMapper.toResponse(tx)).thenReturn(resp);

            Page<TransactionResponse> result = transactionService.getTransactions(
                    USER_ID, null, null, null, null, null, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("returns empty page when user has no transactions")
        void returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 20);
            when(transactionRepository.findFiltered(USER_ID, null, null, null, null, null, pageable))
                    .thenReturn(Page.empty());

            Page<TransactionResponse> result = transactionService.getTransactions(
                    USER_ID, null, null, null, null, null, pageable);

            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("creates transaction and returns response")
        void createsTransaction() {
            TransactionRequest request = new TransactionRequest();
            request.setType(TransactionType.EXPENSE);
            request.setAmount(new BigDecimal("1500.00"));
            request.setCurrency("ARS");
            request.setCategoryId(101L);
            request.setDescription("Compra semanal");
            request.setDate(LocalDate.of(2025, 1, 15));

            Transaction saved = buildTransaction(10L, USER_ID);
            TransactionResponse resp = buildResponse(saved);

            doNothing().when(categoryService).validateSubcategoryForTransaction(101L, USER_ID);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);
            when(transactionRepository.findById(10L)).thenReturn(Optional.of(saved));
            when(transactionMapper.toResponse(saved)).thenReturn(resp);

            TransactionResponse result = transactionService.create(USER_ID, request);

            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getAmount()).isEqualByComparingTo("1500.00");
            verify(categoryService).validateSubcategoryForTransaction(101L, USER_ID);
            verify(transactionRepository).save(any(Transaction.class));
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when category is invalid")
        void throwsWhenCategoryInvalid() {
            TransactionRequest request = new TransactionRequest();
            request.setCategoryId(999L);
            request.setType(TransactionType.EXPENSE);
            request.setAmount(BigDecimal.TEN);
            request.setCurrency("ARS");
            request.setDate(LocalDate.now());

            doThrow(new ResourceNotFoundException("Category", 999L))
                    .when(categoryService).validateSubcategoryForTransaction(999L, USER_ID);

            assertThatThrownBy(() -> transactionService.create(USER_ID, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("returns transaction when found and owned by user")
        void returnsTransaction() {
            Transaction tx = buildTransaction(5L, USER_ID);
            TransactionResponse resp = buildResponse(tx);

            when(transactionRepository.findById(5L)).thenReturn(Optional.of(tx));
            when(transactionMapper.toResponse(tx)).thenReturn(resp);

            TransactionResponse result = transactionService.getById(5L, USER_ID);

            assertThat(result.getId()).isEqualTo(5L);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when transaction not found")
        void throwsWhenNotFound() {
            when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.getById(99L, USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when transaction belongs to another user")
        void throwsWhenWrongUser() {
            Transaction tx = buildTransaction(5L, OTHER_USER_ID);
            when(transactionRepository.findById(5L)).thenReturn(Optional.of(tx));

            assertThatThrownBy(() -> transactionService.getById(5L, USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("updates transaction fields and returns response")
        void updatesTransaction() {
            Transaction existing = buildTransaction(5L, USER_ID);
            TransactionRequest request = new TransactionRequest();
            request.setType(TransactionType.INCOME);
            request.setAmount(new BigDecimal("2000.00"));
            request.setCurrency("USD");
            request.setCategoryId(901L);
            request.setDescription("Sueldo");
            request.setDate(LocalDate.of(2025, 2, 1));

            TransactionResponse resp = TransactionResponse.builder()
                    .id(5L).type("INCOME").amount(new BigDecimal("2000.00")).build();

            when(transactionRepository.findById(5L)).thenReturn(Optional.of(existing));
            doNothing().when(categoryService).validateSubcategoryForTransaction(901L, USER_ID);
            when(transactionRepository.save(existing)).thenReturn(existing);
            when(transactionMapper.toResponse(existing)).thenReturn(resp);

            TransactionResponse result = transactionService.update(5L, USER_ID, request);

            assertThat(result.getType()).isEqualTo("INCOME");
            verify(transactionRepository).save(existing);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when transaction not owned by user")
        void throwsWhenNotOwned() {
            Transaction tx = buildTransaction(5L, OTHER_USER_ID);
            TransactionRequest request = new TransactionRequest();

            when(transactionRepository.findById(5L)).thenReturn(Optional.of(tx));

            assertThatThrownBy(() -> transactionService.update(5L, USER_ID, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("deletes transaction when owned by user")
        void deletesTransaction() {
            Transaction tx = buildTransaction(5L, USER_ID);
            when(transactionRepository.findById(5L)).thenReturn(Optional.of(tx));

            transactionService.delete(5L, USER_ID);

            verify(transactionRepository).delete(tx);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when not owned by user")
        void throwsWhenNotOwned() {
            Transaction tx = buildTransaction(5L, OTHER_USER_ID);
            when(transactionRepository.findById(5L)).thenReturn(Optional.of(tx));

            assertThatThrownBy(() -> transactionService.delete(5L, USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(transactionRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("getSummary")
    class GetSummary {

        @Test
        @DisplayName("builds summary for all currencies when none specified")
        void buildsAllCurrencySummary() {
            LocalDate from = LocalDate.of(2025, 1, 1);
            LocalDate to = LocalDate.of(2025, 1, 31);

            // ARS
            when(transactionRepository.sumByTypeAndCurrency(USER_ID, TransactionType.INCOME, "ARS", from, to))
                    .thenReturn(new BigDecimal("50000"));
            when(transactionRepository.sumByTypeAndCurrency(USER_ID, TransactionType.EXPENSE, "ARS", from, to))
                    .thenReturn(new BigDecimal("20000"));
            when(loanInstallmentRepository.sumPaidByUserAndCurrencyAndPaidDateRange(USER_ID, "ARS", from, to))
                    .thenReturn(new BigDecimal("5000"));
            when(cardExpenseInstallmentRepository.sumPaidByUserAndCurrencyAndPaidDateRange(USER_ID, "ARS", from, to))
                    .thenReturn(new BigDecimal("3000"));
            when(loanRepository.countActiveByUserIdAndCurrency(USER_ID, "ARS")).thenReturn(2);
            when(loanRepository.sumRemainingDebtByUserIdAndCurrency(USER_ID, "ARS"))
                    .thenReturn(new BigDecimal("100000"));
            when(cardExpenseRepository.countActiveByUserIdAndCurrency(USER_ID, "ARS")).thenReturn(1);
            when(cardExpenseRepository.sumRemainingDebtByUserIdAndCurrency(USER_ID, "ARS"))
                    .thenReturn(new BigDecimal("15000"));

            // USD
            when(transactionRepository.sumByTypeAndCurrency(USER_ID, TransactionType.INCOME, "USD", from, to))
                    .thenReturn(BigDecimal.ZERO);
            when(transactionRepository.sumByTypeAndCurrency(USER_ID, TransactionType.EXPENSE, "USD", from, to))
                    .thenReturn(BigDecimal.ZERO);
            when(loanInstallmentRepository.sumPaidByUserAndCurrencyAndPaidDateRange(USER_ID, "USD", from, to))
                    .thenReturn(BigDecimal.ZERO);
            when(cardExpenseInstallmentRepository.sumPaidByUserAndCurrencyAndPaidDateRange(USER_ID, "USD", from, to))
                    .thenReturn(BigDecimal.ZERO);
            when(loanRepository.countActiveByUserIdAndCurrency(USER_ID, "USD")).thenReturn(0);
            when(loanRepository.sumRemainingDebtByUserIdAndCurrency(USER_ID, "USD")).thenReturn(BigDecimal.ZERO);
            when(cardExpenseRepository.countActiveByUserIdAndCurrency(USER_ID, "USD")).thenReturn(0);
            when(cardExpenseRepository.sumRemainingDebtByUserIdAndCurrency(USER_ID, "USD")).thenReturn(BigDecimal.ZERO);

            List<SummaryResponse> result = transactionService.getSummary(USER_ID, null, from, to);

            assertThat(result).hasSize(2);
            SummaryResponse ars = result.stream().filter(s -> "ARS".equals(s.getCurrency())).findFirst().orElseThrow();
            assertThat(ars.getTotalIncome()).isEqualByComparingTo("50000");
            assertThat(ars.getTotalExpense()).isEqualByComparingTo("28000"); // 20000+5000+3000
            assertThat(ars.getBalance()).isEqualByComparingTo("22000");
            assertThat(ars.getActiveLoans()).isEqualTo(2);
        }

        @Test
        @DisplayName("builds summary for single currency when specified")
        void buildsSingleCurrencySummary() {
            LocalDate from = LocalDate.of(2025, 1, 1);
            LocalDate to = LocalDate.of(2025, 1, 31);

            when(transactionRepository.sumByTypeAndCurrency(USER_ID, TransactionType.INCOME, "USD", from, to))
                    .thenReturn(new BigDecimal("1000"));
            when(transactionRepository.sumByTypeAndCurrency(USER_ID, TransactionType.EXPENSE, "USD", from, to))
                    .thenReturn(new BigDecimal("400"));
            when(loanInstallmentRepository.sumPaidByUserAndCurrencyAndPaidDateRange(USER_ID, "USD", from, to))
                    .thenReturn(BigDecimal.ZERO);
            when(cardExpenseInstallmentRepository.sumPaidByUserAndCurrencyAndPaidDateRange(USER_ID, "USD", from, to))
                    .thenReturn(BigDecimal.ZERO);
            when(loanRepository.countActiveByUserIdAndCurrency(USER_ID, "USD")).thenReturn(0);
            when(loanRepository.sumRemainingDebtByUserIdAndCurrency(USER_ID, "USD")).thenReturn(BigDecimal.ZERO);
            when(cardExpenseRepository.countActiveByUserIdAndCurrency(USER_ID, "USD")).thenReturn(0);
            when(cardExpenseRepository.sumRemainingDebtByUserIdAndCurrency(USER_ID, "USD")).thenReturn(BigDecimal.ZERO);

            List<SummaryResponse> result = transactionService.getSummary(USER_ID, "USD", from, to);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCurrency()).isEqualTo("USD");
            assertThat(result.get(0).getBalance()).isEqualByComparingTo("600");
        }

        @Test
        @DisplayName("totalExpense includes loan and card installments")
        void totalExpenseIncludesInstallments() {
            LocalDate from = LocalDate.of(2025, 1, 1);
            LocalDate to = LocalDate.of(2025, 1, 31);

            when(transactionRepository.sumByTypeAndCurrency(USER_ID, TransactionType.INCOME, "ARS", from, to))
                    .thenReturn(BigDecimal.ZERO);
            when(transactionRepository.sumByTypeAndCurrency(USER_ID, TransactionType.EXPENSE, "ARS", from, to))
                    .thenReturn(new BigDecimal("1000"));
            when(loanInstallmentRepository.sumPaidByUserAndCurrencyAndPaidDateRange(USER_ID, "ARS", from, to))
                    .thenReturn(new BigDecimal("500"));
            when(cardExpenseInstallmentRepository.sumPaidByUserAndCurrencyAndPaidDateRange(USER_ID, "ARS", from, to))
                    .thenReturn(new BigDecimal("300"));
            when(loanRepository.countActiveByUserIdAndCurrency(USER_ID, "ARS")).thenReturn(0);
            when(loanRepository.sumRemainingDebtByUserIdAndCurrency(USER_ID, "ARS")).thenReturn(BigDecimal.ZERO);
            when(cardExpenseRepository.countActiveByUserIdAndCurrency(USER_ID, "ARS")).thenReturn(0);
            when(cardExpenseRepository.sumRemainingDebtByUserIdAndCurrency(USER_ID, "ARS")).thenReturn(BigDecimal.ZERO);

            List<SummaryResponse> result = transactionService.getSummary(USER_ID, "ARS", from, to);

            assertThat(result.get(0).getTotalExpense()).isEqualByComparingTo("1800");
        }
    }
}
