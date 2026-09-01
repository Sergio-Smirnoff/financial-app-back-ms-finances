package com.financialapp.finances.application.transaction;

import com.financialapp.commons.core.domain.model.Cbu;
import com.financialapp.finances.application.transaction.impl.SearchTransactionsUseCaseImpl;
import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.Money;
import com.financialapp.finances.domain.common.model.TransactionId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchTransactionsUseCaseImplTest {

    @Mock
    private TransactionRepository repository;

    @InjectMocks
    private SearchTransactionsUseCaseImpl useCase;

    private Transaction sampleTransaction() {
        return Transaction.reconstitute(
                new TransactionId(1L),
                new UserId(7L),
                new Cbu("0001112223334445556667"),
                new Cbu("9998887776665554443332"),
                new Money(new BigDecimal("12500.00"), Currency.getInstance("ARS")),
                new CategoryId(2L),
                "Supermercado",
                LocalDate.of(2026, 7, 4)
        );
    }

    @Test
    void searchesByDescriptionCaseInsensitive() {
        UserId user = new UserId(7L);
        when(repository.searchByDescription(user, "super", 10)).thenReturn(List.of(sampleTransaction()));

        List<Transaction> found = useCase.execute(user, "super", 10);

        assertThat(found).hasSize(1);
        verify(repository).searchByDescription(user, "super", 10);
    }

    @Test
    void rejectsBlankQuery() {
        assertThatThrownBy(() -> useCase.execute(new UserId(7L), "  ", 10))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNullQuery() {
        assertThatThrownBy(() -> useCase.execute(new UserId(7L), null, 10))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
