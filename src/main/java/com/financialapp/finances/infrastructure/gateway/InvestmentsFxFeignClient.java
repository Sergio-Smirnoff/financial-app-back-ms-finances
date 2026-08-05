package com.financialapp.finances.infrastructure.gateway;

import com.financialapp.finances.infrastructure.gateway.dto.FxSnapshotRatesResponseDto;
import com.financialapp.finances.infrastructure.gateway.dto.GatewayApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@FeignClient(name = "ms-investments", url = "${investments.service.url:http://localhost:8086}")
public interface InvestmentsFxFeignClient {

    @GetMapping("/api/v1/investments/fx/rates/at")
    GatewayApiResponse<FxSnapshotRatesResponseDto> getRatesAtDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date);
}
