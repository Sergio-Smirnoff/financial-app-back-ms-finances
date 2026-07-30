package com.financialapp.finances.application.budget.impl;

import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.Money;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.gateway.AccountOwnershipGateway;
import com.financialapp.finances.domain.model.budget.Budget;
import com.financialapp.finances.domain.model.budget.BudgetPeriod;
import com.financialapp.finances.domain.model.category.CategoryNames;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.model.transaction.TransactionKind;
import com.financialapp.finances.domain.repository.BudgetRepository;
import com.financialapp.finances.domain.repository.CategoryRepository;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.domain.service.TransactionClassifier;
import com.financialapp.finances.domain.usecase.budget.BudgetPaceView;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetBudgetPaceUseCaseImplTest {

    private final BudgetRepository budgetRepository = mock(BudgetRepository.class);
    private final TransactionRepository transactionRepository = mock(TransactionRepository.class);
    private final AccountOwnershipGateway ownershipGateway = mock(AccountOwnershipGateway.class);
    private final TransactionClassifier classifier = mock(TransactionClassifier.class);
    private final CategoryRepository categoryRepository = mock(CategoryRepository.class);

    private final GetBudgetPaceUseCaseImpl useCase = new GetBudgetPaceUseCaseImpl(
            budgetRepository, transactionRepository, ownershipGateway, classifier, categoryRepository);

    @Test
    void computesPacePerBudgetOnlyForMatchingExpenseTransactions() {
        UserId userId = new UserId(42L);
        BudgetPeriod period = new BudgetPeriod(2026, 7);
        Currency ARS = Currency.getInstance("ARS");

        Budget budget = Budget.create(userId, new CategoryId(10L), period, new Money(new BigDecimal("100000.00"), ARS), null);
        when(budgetRepository.findByUserAndPeriod(userId, period)).thenReturn(List.of(budget));

        Cbu ownedCbu = new Cbu("0001112223334445556667");
        Cbu extCbu = new Cbu("9998887776665554443332");
        when(ownershipGateway.ownedAccounts(userId)).thenReturn(Set.of(ownedCbu));

        Transaction txExpense = Transaction.create(userId, ownedCbu, extCbu, new Money(new BigDecimal("30000.00"), ARS), new CategoryId(10L), "x", LocalDate.of(2026, 7, 10));
        when(transactionRepository.findByUserAndDateBetween(eq(userId), any(), any())).thenReturn(List.of(txExpense));
        when(classifier.classify(txExpense, Set.of(ownedCbu))).thenReturn(TransactionKind.EXPENSE);
        when(categoryRepository.findNamesById(new CategoryId(10L))).thenReturn(Optional.of(new CategoryNames("Food", null)));

        List<BudgetPaceView> result = useCase.execute(userId, period, LocalDate.of(2026, 7, 15));

        assertThat(result).hasSize(1);
        BudgetPaceView view = result.get(0);
        assertThat(view.categoryName()).isEqualTo("Food");
        assertThat(view.pace().spent()).isEqualTo(new Money(new BigDecimal("30000.00"), ARS));
        assertThat(view.pace().remaining()).isEqualTo(new Money(new BigDecimal("70000.00"), ARS));
        assertThat(view.pace().pctUsed()).isEqualTo(new BigDecimal("30.00"));
    }
}
