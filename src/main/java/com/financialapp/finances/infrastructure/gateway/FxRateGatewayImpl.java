package com.financialapp.finances.infrastructure.gateway;

import com.financialapp.finances.domain.gateway.FxRateGateway;
import com.financialapp.finances.domain.model.transaction.FxSnapshot;
import com.financialapp.finances.infrastructure.gateway.dto.FxSnapshotRatesResponseDto;
import com.financialapp.finances.infrastructure.gateway.dto.GatewayApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class FxRateGatewayImpl implements FxRateGateway {

    private final InvestmentsFxFeignClient investmentsFxFeignClient;

    @Override
    public Optional<FxSnapshot> getRatesForDate(LocalDate date) {
        try {
            GatewayApiResponse<FxSnapshotRatesResponseDto> response = investmentsFxFeignClient.getRatesAtDate(date);
            if (response != null && response.data() != null) {
                FxSnapshotRatesResponseDto data = response.data();
                LocalDate rateDate = data.rateDate() != null ? data.rateDate() : date;
                return Optional.of(new FxSnapshot(
                        data.mepRate(),
                        data.cclRate(),
                        data.oficialRate(),
                        rateDate
                ));
            }
        } catch (Exception e) {
            log.warn("Failed to fetch FX rates from ms-investments for date {}: {}", date, e.getMessage());
        }
        return Optional.empty();
    }
}
