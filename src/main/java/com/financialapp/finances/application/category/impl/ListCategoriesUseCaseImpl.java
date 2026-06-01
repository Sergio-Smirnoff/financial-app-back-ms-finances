package com.financialapp.finances.application.category.impl;

import com.financialapp.finances.domain.common.model.CategoryStatus;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.category.Category;
import com.financialapp.finances.domain.repository.CategoryRepository;
import com.financialapp.finances.domain.usecase.category.ListCategories;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListCategoriesUseCaseImpl implements ListCategories {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Category> execute(UserId userId) {
        return categoryRepository.findAllOwnedBy(userId).stream()
                .filter(category -> category.status() == CategoryStatus.ACTIVE)
                .toList();
    }
}
