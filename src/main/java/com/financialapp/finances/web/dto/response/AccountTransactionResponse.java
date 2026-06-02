package com.financialapp.finances.web.dto.response;

import java.time.LocalDate;

/** Per-account view consumed by ms-banks: amount is signedFor(accountCbu). Field names are the contract. */
public record AccountTransactionResponse(
        Long transactionId,
        String accountCbu,
        String amount,
        String currency,
        String description,
        String category,
        String subcategory,
        LocalDate date) {}
