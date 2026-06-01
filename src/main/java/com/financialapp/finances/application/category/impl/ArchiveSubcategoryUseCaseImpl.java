package com.financialapp.finances.application.category.impl;

import com.financialapp.finances.domain.model.category.Category;
import com.financialapp.finances.domain.repository.CategoryRepository;
import com.financialapp.finances.domain.usecase.category.ArchiveSubcategory;
import com.financialapp.finances.domain.usecase.category.command.ArchiveSubcategoryCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArchiveSubcategoryUseCaseImpl implements ArchiveSubcategory {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public Category execute(ArchiveSubcategoryCommand command) {
        Category category = categoryRepository.findByIdOwnedBy(command.categoryId(), command.userId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "category " + command.categoryId().value() + " not found for user"));
        return categoryRepository.save(category.archiveSubcategory(command.subId()));
    }
}
