package com.financialapp.finances.application.transaction;

import com.financialapp.finances.application.transaction.impl.GetMonthlyFlowUseCaseImpl;
import com.financialapp.finances.domain.common.model.DateRange;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.transaction.MonthlyFlow;
import com.financialapp.finances.domain.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetMonthlyFlowUseCaseImplTest {

    @Mock
    private TransactionRepository repository;

    @InjectMocks
    private GetMonthlyFlowUseCaseImpl useCase;

    private static final Currency ARS = Currency.getInstance("ARS");

    @Test
    void returnsContiguousSeriesFillingMissingMonthsWithZero() {
        UserId user = new UserId(1L);
        DateRange range = new DateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31));

        List<MonthlyFlow> raw = List.of(
                new MonthlyFlow(YearMonth.of(2026, 1), ARS, new BigDecimal("1000.00"), new BigDecimal("200.00")),
                new MonthlyFlow(YearMonth.of(2026, 3), ARS, new BigDecimal("1500.00"), new BigDecimal("300.00"))
        );

        when(repository.monthlyFlow(user, range)).thenReturn(raw);

        List<MonthlyFlow> result = useCase.execute(user, range);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).month()).isEqualTo(YearMonth.of(2026, 1));
        assertThat(result.get(0).income()).isEqualByComparingTo("1000.00");
        assertThat(result.get(0).expense()).isEqualByComparingTo("200.00");

        assertThat(result.get(1).month()).isEqualTo(YearMonth.of(2026, 2));
        assertThat(result.get(1).income()).isEqualByComparingTo("0");
        assertThat(result.get(1).expense()).isEqualByComparingTo("0");

        assertThat(result.get(2).month()).isEqualTo(YearMonth.of(2026, 3));
        assertThat(result.get(2).income()).isEqualByComparingTo("1500.00");
        assertThat(result.get(2).expense()).isEqualByComparingTo("300.00");
    }

    @Test
    void rejectsNullParameters() {
        assertThatThrownBy(() -> useCase.execute(null, new DateRange(LocalDate.now(), LocalDate.now())))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> useCase.execute(new UserId(1L), null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
