package com.financialapp.finances.domain.gateway;

import com.financialapp.finances.domain.model.transaction.FxSnapshot;

import java.time.LocalDate;
import java.util.Optional;

public interface FxRateGateway {
    Optional<FxSnapshot> getRatesForDate(LocalDate date);
}
