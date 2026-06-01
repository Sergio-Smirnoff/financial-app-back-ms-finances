package com.financialapp.finances.application.category.impl;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.category.Category;
import com.financialapp.finances.domain.repository.CategoryRepository;
import com.financialapp.finances.domain.usecase.category.GetCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetCategoryUseCaseImpl implements GetCategory {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public Category execute(CategoryId categoryId, UserId userId) {
        return categoryRepository.findByIdOwnedBy(categoryId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "category " + categoryId.value() + " not found for user"));
    }
}
