package com.financialapp.finances.application.budget.impl;

import com.financialapp.commons.core.domain.model.Cbu;
import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.DateRange;
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
import com.financialapp.finances.domain.service.BudgetPace;
import com.financialapp.finances.domain.service.BudgetPaceResult;
import com.financialapp.finances.domain.service.TransactionClassifier;
import com.financialapp.finances.domain.usecase.budget.BudgetPaceView;
import com.financialapp.finances.domain.usecase.budget.GetBudgetPace;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetBudgetPaceUseCaseImpl implements GetBudgetPace {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final AccountOwnershipGateway ownershipGateway;
    private final TransactionClassifier classifier;
    private final CategoryRepository categoryRepository;
    private final BudgetPace budgetPace = new BudgetPace();

    @Override
    @Transactional(readOnly = true)
    public List<BudgetPaceView> execute(UserId userId, BudgetPeriod period, LocalDate today) {
        List<Budget> budgets = budgetRepository.findByUserAndPeriod(userId, period);
        if (budgets.isEmpty()) {
            return List.of();
        }

        DateRange dateRange = period.dateRange();
        List<Transaction> userTransactions = transactionRepository.findByUserAndDateBetween(userId, dateRange.from(), dateRange.to());
        Set<Cbu> owned = ownershipGateway.ownedAccounts(userId);

        Map<CategoryCurrencyKey, BigDecimal> spendMap = userTransactions.stream()
                .filter(tx -> classifier.classify(tx, owned) == TransactionKind.EXPENSE)
                .collect(Collectors.groupingBy(
                        tx -> new CategoryCurrencyKey(tx.categoryId(), tx.currency()),
                        Collectors.reducing(BigDecimal.ZERO, tx -> tx.money().amount(), BigDecimal::add)
                ));

        return budgets.stream().map(budget -> {
            CategoryCurrencyKey key = new CategoryCurrencyKey(budget.categoryId(), budget.amount().currency());
            BigDecimal totalSpendAmount = spendMap.getOrDefault(key, BigDecimal.ZERO);
            Money actualSpend = totalSpendAmount.compareTo(BigDecimal.ZERO) > 0
                    ? new Money(totalSpendAmount, budget.amount().currency())
                    : null;

            BudgetPaceResult paceResult = budgetPace.evaluate(budget, actualSpend, today);
            String categoryName = resolveCategoryName(budget.categoryId());
            return new BudgetPaceView(budget, categoryName, paceResult);
        }).toList();
    }

    private String resolveCategoryName(CategoryId categoryId) {
        CategoryNames names = categoryRepository.findNamesById(categoryId).orElse(new CategoryNames(null, null));
        return names.subcategory() != null ? names.subcategory() : names.category();
    }

    private record CategoryCurrencyKey(CategoryId categoryId, Currency currency) { }
}
