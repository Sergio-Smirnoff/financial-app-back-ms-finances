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

    /**
     * Sentinel CBU for the external/bank side of a bank-originated (installment) transaction.
     * Bank code {@code 000} is structurally impossible and all-zeros fails real CBU check digits,
     * so it can never collide with a genuine account and ms-banks never reports it as owned.
     */
    public static final Cbu EXTERNAL_INSTALLMENT_CBU = new Cbu("0000000000000000000000");

    public Cbu {
        if (cbuNumber == null || !CBU_NUMBER_PATTERN.matcher(cbuNumber).matches()) {
            throw new InvalidCbuException(cbuNumber);
        }
    }
}
