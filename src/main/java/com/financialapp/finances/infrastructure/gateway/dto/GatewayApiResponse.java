package com.financialapp.finances.infrastructure.gateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Minimal view of the shared ApiResponse envelope — only {@code data} is needed here. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GatewayApiResponse<T>(T data) {}
