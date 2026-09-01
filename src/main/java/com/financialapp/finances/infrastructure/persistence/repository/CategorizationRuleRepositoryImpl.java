package com.financialapp.finances.infrastructure.persistence.repository;

import com.financialapp.finances.domain.common.model.RuleId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.rule.CategorizationRule;
import com.financialapp.finances.domain.repository.CategorizationRuleRepository;
import com.financialapp.finances.infrastructure.persistence.entity.CategorizationRuleJpaEntity;
import com.financialapp.finances.infrastructure.persistence.jpa.CategorizationRuleJpaRepository;
import com.financialapp.finances.infrastructure.persistence.mapper.CategorizationRulePersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CategorizationRuleRepositoryImpl implements CategorizationRuleRepository {

    private final CategorizationRuleJpaRepository jpaRepository;
    private final CategorizationRulePersistenceMapper mapper;

    public CategorizationRuleRepositoryImpl(CategorizationRuleJpaRepository jpaRepository,
                                             CategorizationRulePersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public CategorizationRule save(CategorizationRule rule) {
        CategorizationRuleJpaEntity entity = mapper.toEntity(rule);
        CategorizationRuleJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<CategorizationRule> findByUser(UserId userId) {
        return jpaRepository.findByUserId(userId.value()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<CategorizationRule> findByIdOwnedBy(RuleId ruleId, UserId userId) {
        return jpaRepository.findByIdAndUserId(ruleId.value(), userId.value())
                .map(mapper::toDomain);
    }

    @Override
    public void delete(CategorizationRule rule) {
        if (rule.id() != null) {
            jpaRepository.deleteById(rule.id().value());
        }
    }
}
