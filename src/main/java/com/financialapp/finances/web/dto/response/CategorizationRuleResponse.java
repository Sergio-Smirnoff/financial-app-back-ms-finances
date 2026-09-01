package com.financialapp.finances.web.dto.response;

import java.time.LocalDateTime;

public record CategorizationRuleResponse(
        Long id,
        String matchType,
        String pattern,
        Long categoryId,
        String categoryName,
        int matchCount,
        LocalDateTime createdAt
) { }
