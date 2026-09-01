package com.financialapp.finances.infrastructure.persistence.mapper;

import com.financialapp.finances.domain.common.model.BudgetId;
import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.Money;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.budget.Budget;
import com.financialapp.finances.domain.model.budget.BudgetPeriod;
import com.financialapp.finances.infrastructure.persistence.entity.BudgetJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class BudgetPersistenceMapper {

    public BudgetJpaEntity toEntity(Budget domain) {
        if (domain == null) return null;
        return BudgetJpaEntity.builder()
                .id(domain.id() != null ? domain.id().value() : null)
                .userId(domain.userId().value())
                .categoryId(domain.categoryId().value())
                .year(domain.period().year())
                .month(domain.period().month())
                .amount(domain.amount().amount())
                .currency(domain.amount().currency().getCurrencyCode())
                .alertThresholdPct(domain.alertThresholdPct())
                .lastAlertedYear(domain.lastAlertedPeriod() != null ? domain.lastAlertedPeriod().year() : null)
                .lastAlertedMonth(domain.lastAlertedPeriod() != null ? domain.lastAlertedPeriod().month() : null)
                .build();
    }

    public Budget toDomain(BudgetJpaEntity entity) {
        if (entity == null) return null;
        BudgetPeriod lastAlerted = null;
        if (entity.getLastAlertedYear() != null && entity.getLastAlertedMonth() != null) {
            lastAlerted = new BudgetPeriod(entity.getLastAlertedYear(), entity.getLastAlertedMonth());
        }
        return Budget.reconstitute(
                new BudgetId(entity.getId()),
                new UserId(entity.getUserId()),
                new CategoryId(entity.getCategoryId()),
                new BudgetPeriod(entity.getYear(), entity.getMonth()),
                new Money(entity.getAmount(), Currency.getInstance(entity.getCurrency())),
                entity.getAlertThresholdPct(),
                lastAlerted
        );
    }
}
