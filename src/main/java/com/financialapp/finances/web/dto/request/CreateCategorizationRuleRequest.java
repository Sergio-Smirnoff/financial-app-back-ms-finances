package com.financialapp.finances.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCategorizationRuleRequest(
        @NotBlank String matchType,
        @NotBlank String pattern,
        @NotNull Long categoryId
) { }
