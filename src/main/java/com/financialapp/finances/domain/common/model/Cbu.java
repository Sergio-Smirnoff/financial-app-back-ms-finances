package com.financialapp.finances.domain.common.model;

import com.financialapp.finances.domain.exception.InvalidCbuException;

import java.util.regex.Pattern;

/**
 * Argentine CBU (Clave Bancaria Uniforme): exactly 22 digits. Reifies the {@code Long accountId}
 * primitive used across the legacy code and aligns Finances with the banks↔finances contract,
 * which keys accounts by CBU string.
 */
public record Cbu(String cbuNumber) {

    private static final Pattern CBU_NUMBER_PATTERN = Pattern.compile("\\d{22}");

    public Cbu {
        if (cbuNumber == null || !CBU_NUMBER_PATTERN.matcher(cbuNumber).matches()) {
            throw new InvalidCbuException(cbuNumber);
        }
    }
}
