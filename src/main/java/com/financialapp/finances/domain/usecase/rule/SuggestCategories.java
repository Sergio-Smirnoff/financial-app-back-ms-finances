package com.financialapp.finances.domain.usecase.rule;

import com.financialapp.finances.domain.common.model.UserId;

import java.util.List;

public interface SuggestCategories {
    List<CategorySuggestion> execute(UserId userId, List<String> descriptions);
}
