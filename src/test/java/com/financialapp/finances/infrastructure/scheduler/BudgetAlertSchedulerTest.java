package com.financialapp.finances.infrastructure.scheduler;

import com.financialapp.commons.core.domain.model.Cbu;
import com.financialapp.finances.domain.common.model.*;
import com.financialapp.finances.domain.event.BudgetThresholdReached;
import com.financialapp.finances.domain.gateway.AccountOwnershipGateway;
import com.financialapp.finances.domain.gateway.DomainEventPublisher;
import com.financialapp.finances.domain.model.budget.Budget;
import com.financialapp.finances.domain.model.budget.BudgetPeriod;
import com.financialapp.finances.domain.model.transaction.Transaction;

import com.financialapp.finances.domain.repository.BudgetRepository;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.domain.service.TransactionClassifier;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BudgetAlertSchedulerTest {

    private static final Currency ARS = Currency.getInstance("ARS");
    private final BudgetRepository budgetRepository = mock(BudgetRepository.class);
    private final TransactionRepository transactionRepository = mock(TransactionRepository.class);
    private final AccountOwnershipGateway ownershipGateway = mock(AccountOwnershipGateway.class);
    private final TransactionClassifier classifier = mock(TransactionClassifier.class);
    private final DomainEventPublisher domainEventPublisher = mock(DomainEventPublisher.class);

    private final BudgetAlertScheduler scheduler = new BudgetAlertScheduler(
            budgetRepository, transactionRepository, ownershipGateway, classifier, domainEventPublisher);

    @Test
    void emitsAlertWhenThresholdReachedAndNotYetAlerted() {
        BudgetPeriod currentPeriod = BudgetPeriod.from(LocalDate.now());
        UserId userId = new UserId(1L);
        CategoryId categoryId = new CategoryId(10L);
        Cbu fromCbu = new Cbu("0001112223334445556667");
        Cbu toCbu = new Cbu("9998887776665554443332");

        Budget budget = Budget.reconstitute(
                new BudgetId(100L), userId, categoryId, currentPeriod,
                new Money(new BigDecimal("1000.00"), ARS), new BigDecimal("80.00"), null);

        when(budgetRepository.findByPeriod(currentPeriod)).thenReturn(List.of(budget));
        when(ownershipGateway.ownedAccounts(userId)).thenReturn(Set.of(fromCbu));

        Transaction tx = Transaction.create(
                userId, fromCbu, toCbu, new Money(new BigDecimal("850.00"), ARS),
                categoryId, "Supermarket", LocalDate.now());
        when(transactionRepository.findByUserAndDateBetween(eq(userId), any(), any()))
                .thenReturn(List.of(tx));
        when(classifier.classify(eq(tx), any())).thenReturn(com.financialapp.finances.domain.model.transaction.TransactionKind.EXPENSE);

        scheduler.evaluateBudgetAlerts();

        verify(budgetRepository).upsert(argThat(b -> currentPeriod.equals(b.lastAlertedPeriod())));

        ArgumentCaptor<BudgetThresholdReached> captor = ArgumentCaptor.forClass(BudgetThresholdReached.class);
        verify(domainEventPublisher).publish(captor.capture());

        BudgetThresholdReached event = captor.getValue();
        assertThat(event.userId()).isEqualTo(userId);
        assertThat(event.budgetId()).isEqualTo(new BudgetId(100L));
        assertThat(event.pctUsed()).isEqualByComparingTo("85.00");
    }

    @Test
    void skipsAlertWhenAlreadyAlertedThisPeriod() {
        BudgetPeriod currentPeriod = BudgetPeriod.from(LocalDate.now());
        UserId userId = new UserId(1L);
        CategoryId categoryId = new CategoryId(10L);

        Budget budget = Budget.reconstitute(
                new BudgetId(100L), userId, categoryId, currentPeriod,
                new Money(new BigDecimal("1000.00"), ARS), new BigDecimal("80.00"), currentPeriod);

        when(budgetRepository.findByPeriod(currentPeriod)).thenReturn(List.of(budget));

        scheduler.evaluateBudgetAlerts();

        verify(budgetRepository, never()).upsert(any());
        verify(domainEventPublisher, never()).publish(any());
    }
}
