package com.financialapp.finances.web.dto.response;

public record CategorySpendResponse(Long categoryId, String categoryName,
                                     String total, String currency, long transactionCount) {
}
