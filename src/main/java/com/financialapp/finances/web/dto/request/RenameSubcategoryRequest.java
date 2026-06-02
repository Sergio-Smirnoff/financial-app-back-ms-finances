package com.financialapp.finances.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RenameSubcategoryRequest(@NotBlank String name) {}
