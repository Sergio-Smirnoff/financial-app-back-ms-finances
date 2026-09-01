package com.financialapp.finances.infrastructure.persistence.jpa;

import com.financialapp.finances.infrastructure.persistence.entity.CategorizationRuleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategorizationRuleJpaRepository extends JpaRepository<CategorizationRuleJpaEntity, Long> {
    List<CategorizationRuleJpaEntity> findByUserId(Long userId);
    Optional<CategorizationRuleJpaEntity> findByIdAndUserId(Long id, Long userId);
}
