package com.financialapp.finances.infrastructure.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

/**
 * The runtime whitelist of currencies this service accepts on write paths. Bound from
 * {@code finances.currencies.supported}. Validated twice: this syntactic JSR-303 check at binding
 * time, and a semantic {@code Currency.getInstance} check in {@code SupportedCurrenciesImpl} at boot.
 * Empty list fails closed (the app refuses to boot).
 */
@Validated
@ConfigurationProperties(prefix = "finances.currencies")
public record CurrenciesProperties(
        @NotEmpty Set<@Pattern(regexp = "[A-Z]{3}") String> supported
) {}
