package com.financialapp.finances.domain.model.rule;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.RuleId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.exception.rule.InvalidCategorizationRuleException;

import java.time.LocalDateTime;
import java.util.Objects;

public final class CategorizationRule {

    private static final int MAX_PATTERN_LENGTH = 200;

    private final RuleId id;
    private final UserId userId;
    private final RuleMatchType matchType;
    private final String pattern;
    private final CategoryId categoryId;
    private final int matchCount;
    private final LocalDateTime createdAt;

    private CategorizationRule(RuleId id, UserId userId, RuleMatchType matchType, String pattern,
                               CategoryId categoryId, int matchCount, LocalDateTime createdAt) {
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.matchType = Objects.requireNonNull(matchType, "matchType must not be null");
        this.categoryId = Objects.requireNonNull(categoryId, "categoryId must not be null");
        Objects.requireNonNull(pattern, "pattern must not be null");
        if (pattern.isBlank() || pattern.trim().length() > MAX_PATTERN_LENGTH) {
            throw new InvalidCategorizationRuleException("pattern must be non-blank and at most " + MAX_PATTERN_LENGTH + " characters");
        }
        if (matchCount < 0) {
            throw new InvalidCategorizationRuleException("matchCount must be non-negative");
        }
        this.id = id;
        this.pattern = pattern.trim();
        this.matchCount = matchCount;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public static CategorizationRule create(UserId userId, RuleMatchType matchType, String pattern, CategoryId categoryId) {
        return new CategorizationRule(null, userId, matchType, pattern, categoryId, 0, LocalDateTime.now());
    }

    public static CategorizationRule reconstitute(RuleId id, UserId userId, RuleMatchType matchType, String pattern,
                                                   CategoryId categoryId, int matchCount, LocalDateTime createdAt) {
        return new CategorizationRule(Objects.requireNonNull(id, "id must not be null"),
                userId, matchType, pattern, categoryId, matchCount, createdAt);
    }

    public boolean matches(String description) {
        if (description == null || description.isBlank()) {
            return false;
        }
        if (matchType == RuleMatchType.CONTAINS) {
            return description.toLowerCase().contains(pattern.toLowerCase());
        }
        if (matchType == RuleMatchType.MERCHANT_EXACT) {
            return description.trim().equalsIgnoreCase(pattern);
        }
        return false;
    }

    public CategorizationRule recordMatch() {
        return new CategorizationRule(id, userId, matchType, pattern, categoryId, matchCount + 1, createdAt);
    }

    public RuleId id() { return id; }
    public UserId userId() { return userId; }
    public RuleMatchType matchType() { return matchType; }
    public String pattern() { return pattern; }
    public CategoryId categoryId() { return categoryId; }
    public int matchCount() { return matchCount; }
    public LocalDateTime createdAt() { return createdAt; }
}
