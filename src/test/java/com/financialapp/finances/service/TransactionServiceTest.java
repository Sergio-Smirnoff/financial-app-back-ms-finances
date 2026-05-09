package com.financialapp.finances.service;

import com.financialapp.finances.exception.ResourceNotFoundException;
import com.financialapp.finances.mapper.TransactionMapper;
import com.financialapp.finances.model.dto.request.TransactionRequest;
import com.financialapp.finances.model.dto.response.SummaryResponse;
import com.financialapp.finances.model.dto.response.TransactionResponse;
import com.financialapp.finances.model.entity.Category;
import com.financialapp.finances.model.entity.Transaction;
import com.financialapp.finances.model.enums.TransactionType;
import com.financialapp.finances.repository.TransactionRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService")
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private CategoryService categoryService;
    @Mock private TransactionMapper transactionMapper;

    @InjectMocks private TransactionService transactionService;

    private static final Long USER_ID = 1L;

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

            when(transactionRepository.findFiltered(USER_ID, null, null, null, null, null, null, pageable))
                    .thenReturn(page);
            when(transactionMapper.toResponse(tx)).thenReturn(resp);

            Page<TransactionResponse> result = transactionService.getTransactions(
                    USER_ID, null, null, null, null, null, null, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
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
            when(transactionMapper.toResponse(saved)).thenReturn(resp);

            TransactionResponse result = transactionService.create(USER_ID, request);

            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getAmount()).isEqualByComparingTo("1500.00");
            verify(categoryService).validateSubcategoryForTransaction(101L, USER_ID);
            verify(transactionRepository).save(any(Transaction.class));
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

            // USD
            when(transactionRepository.sumByTypeAndCurrency(USER_ID, TransactionType.INCOME, "USD", from, to))
                    .thenReturn(BigDecimal.ZERO);
            when(transactionRepository.sumByTypeAndCurrency(USER_ID, TransactionType.EXPENSE, "USD", from, to))
                    .thenReturn(BigDecimal.ZERO);

            List<SummaryResponse> result = transactionService.getSummary(USER_ID, null, from, to);

            assertThat(result).hasSize(2);
            SummaryResponse ars = result.stream().filter(s -> "ARS".equals(s.getCurrency())).findFirst().orElseThrow();
            assertThat(ars.getTotalIncome()).isEqualByComparingTo("50000");
            assertThat(ars.getTotalExpense()).isEqualByComparingTo("20000");
            assertThat(ars.getBalance()).isEqualByComparingTo("30000");
        }
    }
}
