package com.financialapp.finances.domain.model.transaction;

import com.financialapp.finances.domain.exception.InvalidIdentifierException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Base64;

public record CursorPage(String cursorAfter, int size) {

    private static final int DEFAULT_SIZE = 50;
    private static final int MAX_SIZE = 200;

    public CursorPage {
        if (size <= 0) {
            size = DEFAULT_SIZE;
        } else if (size > MAX_SIZE) {
            size = MAX_SIZE;
        }
    }

    public static String encode(LocalDate date, Long id) {
        if (date == null || id == null) {
            return null;
        }
        String raw = date.toString() + "|" + id;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public LocalDate decodedDate() {
        String[] parts = parseCursorParts();
        if (parts == null) return null;
        try {
            return LocalDate.parse(parts[0]);
        } catch (DateTimeParseException e) {
            throw new InvalidIdentifierException("cursor", cursorAfter);
        }
    }

    public Long decodedId() {
        String[] parts = parseCursorParts();
        if (parts == null) return null;
        try {
            return Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            throw new InvalidIdentifierException("cursor", cursorAfter);
        }
    }

    private String[] parseCursorParts() {
        if (cursorAfter == null || cursorAfter.isBlank()) {
            return null;
        }
        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(cursorAfter);
            String raw = new String(decodedBytes, StandardCharsets.UTF_8);
            String[] parts = raw.split("\\|");
            if (parts.length != 2) {
                throw new InvalidIdentifierException("cursor", cursorAfter);
            }
            return parts;
        } catch (Exception e) {
            if (e instanceof InvalidIdentifierException iie) {
                throw iie;
            }
            throw new InvalidIdentifierException("cursor", cursorAfter);
        }
    }
}
