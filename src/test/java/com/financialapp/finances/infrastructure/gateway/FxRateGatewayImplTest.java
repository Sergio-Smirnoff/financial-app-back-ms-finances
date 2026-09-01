package com.financialapp.finances.infrastructure.gateway;

import com.financialapp.finances.domain.model.transaction.FxSnapshot;
import com.financialapp.finances.infrastructure.gateway.dto.FxSnapshotRatesResponseDto;
import com.financialapp.finances.infrastructure.gateway.dto.GatewayApiResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class FxRateGatewayImplTest {

    private final InvestmentsFxFeignClient client = mock(InvestmentsFxFeignClient.class);
    private final FxRateGatewayImpl gateway = new FxRateGatewayImpl(client);

    @Test
    void returnsSnapshotWhenFeignCallSucceeds() {
        LocalDate date = LocalDate.of(2026, 8, 5);
        FxSnapshotRatesResponseDto dto = new FxSnapshotRatesResponseDto(
                new BigDecimal("1200.50"),
                new BigDecimal("1250.00"),
                new BigDecimal("980.10"),
                date
        );
        when(client.getRatesAtDate(date)).thenReturn(new GatewayApiResponse<>(dto));

        Optional<FxSnapshot> result = gateway.getRatesForDate(date);

        assertThat(result).isPresent();
        FxSnapshot snapshot = result.get();
        assertThat(snapshot.mepRate()).isEqualByComparingTo("1200.50");
        assertThat(snapshot.cclRate()).isEqualByComparingTo("1250.00");
        assertThat(snapshot.oficialRate()).isEqualByComparingTo("980.10");
        assertThat(snapshot.rateDate()).isEqualTo(date);
    }

    @Test
    void returnsEmptyOptionalWhenFeignCallFailsWithException() {
        LocalDate date = LocalDate.of(2026, 8, 5);
        when(client.getRatesAtDate(date)).thenThrow(new RuntimeException("Connection timed out"));

        Optional<FxSnapshot> result = gateway.getRatesForDate(date);

        assertThat(result).isEmpty();
    }
}
