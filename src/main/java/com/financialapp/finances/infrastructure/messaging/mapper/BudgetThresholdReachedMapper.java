package com.financialapp.finances.infrastructure.messaging.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.commons.messaging.domain.model.EventType;
import com.financialapp.commons.messaging.domain.model.OutboxRecord;
import com.financialapp.finances.domain.event.BudgetThresholdReached;
import com.financialapp.finances.infrastructure.messaging.payload.BudgetThresholdReachedData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BudgetThresholdReachedMapper extends JsonTypedDomainEventMapper<BudgetThresholdReached> {

    public static final String TOPIC = "finances.budget.threshold_reached";
    static final String SCHEMA = "https://schemas.financial-app/finances/budget-threshold-reached/v1";
    static final String SOURCE = "ms-finances";

    public BudgetThresholdReachedMapper(ObjectMapper objectMapper) {
        super(BudgetThresholdReached.class, objectMapper);
    }

    @Override
    protected List<OutboxRecord> mapTyped(BudgetThresholdReached event) {
        BudgetThresholdReachedData data = new BudgetThresholdReachedData(
                event.budgetId().value(),
                event.userId().value(),
                event.categoryId().value(),
                event.pctUsed(),
                event.alertThresholdPct(),
                event.period().year(),
                event.period().month());

        return List.of(OutboxRecord.create(
                TOPIC,
                String.valueOf(event.budgetId().value()),
                new EventType(TOPIC),
                SOURCE,
                SCHEMA,
                serialize(data)));
    }
}
