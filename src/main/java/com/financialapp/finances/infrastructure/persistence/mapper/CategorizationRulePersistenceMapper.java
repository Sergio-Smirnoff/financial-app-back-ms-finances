package com.financialapp.finances.infrastructure.persistence.mapper;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.RuleId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.rule.CategorizationRule;
import com.financialapp.finances.domain.model.rule.RuleMatchType;
import com.financialapp.finances.infrastructure.persistence.entity.CategorizationRuleJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class CategorizationRulePersistenceMapper {

    public CategorizationRuleJpaEntity toEntity(CategorizationRule domain) {
        if (domain == null) return null;
        return CategorizationRuleJpaEntity.builder()
                .id(domain.id() != null ? domain.id().value() : null)
                .userId(domain.userId().value())
                .matchType(domain.matchType().name())
                .pattern(domain.pattern())
                .categoryId(domain.categoryId().value())
                .matchCount(domain.matchCount())
                .createdAt(domain.createdAt())
                .build();
    }

    public CategorizationRule toDomain(CategorizationRuleJpaEntity entity) {
        if (entity == null) return null;
        return CategorizationRule.reconstitute(
                new RuleId(entity.getId()),
                new UserId(entity.getUserId()),
                RuleMatchType.valueOf(entity.getMatchType()),
                entity.getPattern(),
                new CategoryId(entity.getCategoryId()),
                entity.getMatchCount(),
                entity.getCreatedAt()
        );
    }
}
