package com.financialapp.finances.domain.model.transaction;

public record DescriptionQuery(String text) {

    public DescriptionQuery {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("description query must not be blank");
        }
        text = text.trim();
    }

    public String containsPattern() {
        String escaped = text
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
