package com.financialapp.finances.infrastructure.gateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FxSnapshotRatesResponseDto(
        BigDecimal mepRate,
        BigDecimal cclRate,
        BigDecimal oficialRate,
        LocalDate rateDate
) {}
