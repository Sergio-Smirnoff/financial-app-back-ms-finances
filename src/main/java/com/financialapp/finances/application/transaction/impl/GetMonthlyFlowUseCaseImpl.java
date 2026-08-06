package com.financialapp.finances.application.transaction.impl;

import com.financialapp.finances.domain.common.model.DateRange;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.transaction.MonthlyFlow;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.domain.usecase.transaction.GetMonthlyFlow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GetMonthlyFlowUseCaseImpl implements GetMonthlyFlow {

    private final TransactionRepository transactionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyFlow> execute(UserId userId, DateRange range) {
        if (userId == null || range == null) {
            throw new IllegalArgumentException("userId and range are required");
        }

        List<MonthlyFlow> rawFlows = transactionRepository.monthlyFlow(userId, range);

        YearMonth startMonth = YearMonth.from(range.from());
        YearMonth endMonth = YearMonth.from(range.to());

        Set<Currency> currencies = new LinkedHashSet<>();
        for (MonthlyFlow f : rawFlows) {
            currencies.add(f.currency());
        }
        if (currencies.isEmpty()) {
            currencies.add(Currency.getInstance("ARS"));
        }

        record Key(YearMonth month, Currency currency) {}
        Map<Key, MonthlyFlow> existingMap = new HashMap<>();
        for (MonthlyFlow f : rawFlows) {
            existingMap.put(new Key(f.month(), f.currency()), f);
        }

        List<MonthlyFlow> result = new ArrayList<>();
        for (Currency currency : currencies) {
            YearMonth current = startMonth;
            while (!current.isAfter(endMonth)) {
                Key key = new Key(current, currency);
                MonthlyFlow flow = existingMap.get(key);
                if (flow != null) {
                    result.add(flow);
                } else {
                    result.add(new MonthlyFlow(current, currency, BigDecimal.ZERO, BigDecimal.ZERO));
                }
                current = current.plusMonths(1);
            }
        }

        return result;
    }
}
