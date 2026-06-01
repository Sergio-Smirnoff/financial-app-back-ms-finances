package com.financialapp.finances.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateCategoryRequest(@NotBlank String name) {}
