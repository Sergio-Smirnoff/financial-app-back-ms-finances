package com.financialapp.finances.web.dto.response;

import java.util.List;

public record RulePreviewResponse(
        int wouldMatchCount,
        List<Long> sampleTransactionIds
) { }
