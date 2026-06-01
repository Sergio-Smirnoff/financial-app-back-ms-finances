package com.financialapp.finances.infrastructure.gateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Only the CBU is needed to answer ownership. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BankAccountResponse(String cbu) {}
