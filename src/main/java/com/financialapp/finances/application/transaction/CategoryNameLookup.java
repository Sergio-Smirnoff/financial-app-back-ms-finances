package com.financialapp.finances.application.transaction;

/**
 * Port for resolving a category id to display names (category + subcategory). Lets the web layer
 * label transactions without depending on infrastructure; the JDBC-backed adapter lives in
 * infrastructure. Temporary bridge until the Category aggregate migrates (slice 5).
 */
public interface CategoryNameLookup {

    CategoryNames resolve(Long categoryId);

    record CategoryNames(String category, String subcategory) {}
}
