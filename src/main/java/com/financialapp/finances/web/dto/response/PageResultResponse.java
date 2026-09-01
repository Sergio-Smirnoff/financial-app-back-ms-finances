package com.financialapp.finances.web.dto.response;

import java.util.List;

public record PageResultResponse<T>(
        List<T> content,
        boolean hasNext,
        String nextCursor,
        long totalElements
) { }
