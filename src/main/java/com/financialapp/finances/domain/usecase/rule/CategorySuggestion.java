package com.financialapp.finances.domain.usecase.rule;

import com.financialapp.finances.domain.common.model.CategoryId;

public record CategorySuggestion(String description, CategoryId categoryId) { }
