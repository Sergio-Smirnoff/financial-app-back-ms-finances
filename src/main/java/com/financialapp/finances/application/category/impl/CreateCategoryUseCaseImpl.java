package com.financialapp.finances.application.category.impl;

import com.financialapp.finances.domain.model.category.Category;
import com.financialapp.finances.domain.repository.CategoryRepository;
import com.financialapp.finances.domain.usecase.category.CreateCategory;
import com.financialapp.finances.domain.usecase.category.command.CreateCategoryCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateCategoryUseCaseImpl implements CreateCategory {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public Category execute(CreateCategoryCommand command) {
        return categoryRepository.save(Category.create(command.userId(), command.name()));
    }
}
