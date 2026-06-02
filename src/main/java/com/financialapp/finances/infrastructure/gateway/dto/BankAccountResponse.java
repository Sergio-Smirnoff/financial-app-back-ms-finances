package com.financialapp.finances.infrastructure.gateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** The CBU answers ownership; the currency validates a transaction's currency against the account. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BankAccountResponse(String cbu, String currency) {}
