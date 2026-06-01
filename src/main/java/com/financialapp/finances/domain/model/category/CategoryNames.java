package com.financialapp.finances.domain.model.category;

/**
 * Display labels for a transaction's category reference. When the id points at a subcategory,
 * {@code category} is the parent category name and {@code subcategory} is the child name; a
 * top-level id yields {@code (name, null)}. A read projection — not part of the Category aggregate.
 */
public record CategoryNames(String category, String subcategory) {}
