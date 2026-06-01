package com.financialapp.finances.web.dto.request;

import com.financialapp.finances.domain.gateway.SupportedCurrencies;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

import java.util.Currency;

@RequiredArgsConstructor
public class SupportedCurrencyValidator implements ConstraintValidator<SupportedCurrency, String> {

    private final SupportedCurrencies supported;

    @Override
    public boolean isValid(String code, ConstraintValidatorContext ctx) {
        if (code == null) {
            return true;   // @NotBlank handles absence separately
        }
        try {
            return supported.isSupported(Currency.getInstance(code));
        } catch (IllegalArgumentException e) {
            return false;  // not even a valid ISO code
        }
    }
}
