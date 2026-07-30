package com.financialapp.finances.web.dto.request;

import java.util.List;

public record SuggestCategoriesRequest(
        List<String> descriptions
) { }
