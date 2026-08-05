package com.financialapp.finances.infrastructure.scheduler;

import com.financialapp.commons.core.domain.model.Cbu;
import com.financialapp.finances.domain.common.model.DateRange;
import com.financialapp.finances.domain.common.model.Money;
import com.financialapp.finances.domain.event.BudgetThresholdReached;
import com.financialapp.finances.domain.gateway.AccountOwnershipGateway;
import com.financialapp.finances.domain.gateway.DomainEventPublisher;
import com.financialapp.finances.domain.model.budget.Budget;
import com.financialapp.finances.domain.model.budget.BudgetPeriod;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.model.transaction.TransactionKind;
import com.financialapp.finances.domain.repository.BudgetRepository;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.domain.service.BudgetPace;
import com.financialapp.finances.domain.service.BudgetPaceResult;
import com.financialapp.finances.domain.service.TransactionClassifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class BudgetAlertScheduler {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final AccountOwnershipGateway ownershipGateway;
    private final TransactionClassifier classifier;
    private final DomainEventPublisher domainEventPublisher;
    private final BudgetPace budgetPace = new BudgetPace();

    @Scheduled(cron = "${finances.scheduler.budget-alert.cron:0 0 8 * * *}")
    @Transactional
    public void evaluateBudgetAlerts() {
        LocalDate today = LocalDate.now();
        BudgetPeriod currentPeriod = BudgetPeriod.from(today);
        log.info("Running BudgetAlertScheduler for period {}", currentPeriod);

        List<Budget> budgets = budgetRepository.findByPeriod(currentPeriod);
        if (budgets.isEmpty()) {
            return;
        }

        Map<Long, List<Budget>> userBudgetsMap = budgets.stream()
                .filter(b -> b.alertThresholdPct() != null)
                .collect(Collectors.groupingBy(b -> b.userId().value()));

        for (Map.Entry<Long, List<Budget>> entry : userBudgetsMap.entrySet()) {
            Long userIdVal = entry.getKey();
            List<Budget> userBudgets = entry.getValue();

            Set<Cbu> owned = ownershipGateway.ownedAccounts(userBudgets.get(0).userId());
            DateRange dateRange = currentPeriod.dateRange();
            List<Transaction> userTransactions = transactionRepository.findByUserAndDateBetween(
                    userBudgets.get(0).userId(), dateRange.from(), dateRange.to());

            Map<Long, BigDecimal> spendMap = userTransactions.stream()
                    .filter(tx -> classifier.classify(tx, owned) == TransactionKind.EXPENSE)
                    .collect(Collectors.groupingBy(
                            tx -> tx.categoryId().value(),
                            Collectors.reducing(BigDecimal.ZERO, tx -> tx.money().amount(), BigDecimal::add)
                    ));

            for (Budget budget : userBudgets) {
                if (currentPeriod.equals(budget.lastAlertedPeriod())) {
                    continue;
                }

                BigDecimal totalSpend = spendMap.getOrDefault(budget.categoryId().value(), BigDecimal.ZERO);
                Money actualSpend = totalSpend.compareTo(BigDecimal.ZERO) > 0
                        ? new Money(totalSpend, budget.amount().currency())
                        : null;

                BudgetPaceResult pace = budgetPace.evaluate(budget, actualSpend, today);
                if (pace.pctUsed().compareTo(budget.alertThresholdPct()) >= 0) {
                    log.info("Budget threshold reached for userId={}, budgetId={}, pctUsed={}%, threshold={}%",
                            userIdVal, budget.id() != null ? budget.id().value() : null, pace.pctUsed(), budget.alertThresholdPct());

                    Budget updated = budget.markAlerted(currentPeriod);
                    budgetRepository.upsert(updated);

                    BudgetThresholdReached event = new BudgetThresholdReached(
                            budget.id(),
                            budget.userId(),
                            budget.categoryId(),
                            pace.pctUsed(),
                            budget.alertThresholdPct(),
                            currentPeriod
                    );
                    domainEventPublisher.publish(event);
                }
            }
        }
    }
}
