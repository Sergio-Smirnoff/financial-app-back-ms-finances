package com.financialapp.finances.domain.usecase.rule;

import java.util.List;

public record RulePreviewResult(int wouldMatchCount, List<Long> sampleTransactionIds) { }
