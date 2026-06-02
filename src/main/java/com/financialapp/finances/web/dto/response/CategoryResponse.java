package com.financialapp.finances.web.dto.response;

import java.util.List;

public record CategoryResponse(Long id, String name, List<SubcategoryResponse> subcategories) {}
