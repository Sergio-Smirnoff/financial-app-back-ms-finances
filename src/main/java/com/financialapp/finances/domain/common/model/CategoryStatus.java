package com.financialapp.finances.domain.common.model;

/**
 * Lifecycle of a Category. ACTIVE categories appear in pickers; ARCHIVED ones are hidden going
 * forward but still resolve the classification of historical transactions (soft-delete for
 * referenced reference-data). Reified from the legacy {@code active:boolean} so the intent is
 * explicit and the set is extensible.
 */
public enum CategoryStatus {
    ACTIVE,
    ARCHIVED
}
