package com.financialapp.finances.application.category.impl;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.CategoryStatus;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.category.Subcategory;
import com.financialapp.finances.domain.repository.CategoryRepository;
import com.financialapp.finances.domain.usecase.category.ListSubcategories;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListSubcategoriesUseCaseImpl implements ListSubcategories {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Subcategory> execute(CategoryId categoryId, UserId userId) {
        return categoryRepository.findByIdOwnedBy(categoryId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "category " + categoryId.value() + " not found for user"))
                .subcategories().stream()
                .filter(sub -> sub.status() == CategoryStatus.ACTIVE)
                .toList();
    }
}
