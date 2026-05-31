package com.financialapp.finances.domain.common.model;

/**
 * Identity of a Category aggregate. Scalar cross-aggregate reference held by a Transaction —
 * never the Category object graph.
 */
public record CategoryId(Long value) {
    public CategoryId {
        IdentifiersValidator.requirePositiveIdentifier(value, "categoryId");
    }
}
