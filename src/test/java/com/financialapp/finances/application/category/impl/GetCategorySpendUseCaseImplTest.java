package com.financialapp.finances.application.category.impl;

import com.financialapp.finances.domain.common.model.*;
import com.financialapp.finances.domain.gateway.AccountOwnershipGateway;
import com.financialapp.finances.domain.model.category.CategoryNames;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.model.transaction.TransactionKind;
import com.financialapp.finances.domain.repository.CategoryRepository;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.domain.usecase.category.CategorySpend;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetCategorySpendUseCaseImplTest {

    private final TransactionRepository transactionRepository = mock(TransactionRepository.class);
    private final CategoryRepository categoryRepository = mock(CategoryRepository.class);
    private final AccountOwnershipGateway ownershipGateway = mock(AccountOwnershipGateway.class);
    private final GetCategorySpendUseCaseImpl useCase =
            new GetCategorySpendUseCaseImpl(transactionRepository, categoryRepository, ownershipGateway);

    @Test
    void aggregatesCategorySpendFilteredByKind() {
        UserId userId = new UserId(42L);
        Cbu myAccount = new Cbu("0001112223334445556667");
        Cbu externalAccount = new Cbu("9998887776665554443332");
        Currency ARS = Currency.getInstance("ARS");

        // Expense: from myAccount to externalAccount
        Transaction expense = Transaction.create(userId, myAccount, externalAccount,
                new Money(new BigDecimal("150.00"), ARS), new CategoryId(5L), "Expense", LocalDate.now());

        // Income: from externalAccount to myAccount
        Transaction income = Transaction.create(userId, externalAccount, myAccount,
                new Money(new BigDecimal("500.00"), ARS), new CategoryId(6L), "Salary", LocalDate.now());

        when(transactionRepository.findByUser(userId)).thenReturn(List.of(expense, income));
        when(ownershipGateway.ownedAccounts(userId)).thenReturn(Set.of(myAccount));
        when(categoryRepository.findNamesById(new CategoryId(5L))).thenReturn(Optional.of(new CategoryNames("Services", "Electricity")));

        List<CategorySpend> spends = useCase.execute(userId, null, TransactionKind.EXPENSE);

        assertThat(spends).hasSize(1);
        assertThat(spends.get(0).categoryId()).isEqualTo(new CategoryId(5L));
        assertThat(spends.get(0).categoryName()).isEqualTo("Electricity");
        assertThat(spends.get(0).total().amount()).isEqualByComparingTo("150.00");
    }
}
