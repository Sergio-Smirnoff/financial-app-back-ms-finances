package com.financialapp.finances.application.transaction.impl;

import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.model.category.CategoryNames;
import com.financialapp.finances.domain.repository.CategoryRepository;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.domain.usecase.transaction.AccountTransactionView;
import com.financialapp.finances.domain.usecase.transaction.ListAccountTransactions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ListAccountTransactionsUseCaseImpl implements ListAccountTransactions {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AccountTransactionView> execute(Cbu accountCbu, Integer limit, LocalDate from, LocalDate to) {
        return transactionRepository.findByAccount(accountCbu, limit, from, to).stream()
                .map(t -> new AccountTransactionView(t, resolveNames(t.categoryId())))
                .toList();
    }

    private CategoryNames resolveNames(CategoryId categoryId) {
        return categoryRepository.findNamesById(categoryId).orElse(new CategoryNames(null, null));
    }
}
