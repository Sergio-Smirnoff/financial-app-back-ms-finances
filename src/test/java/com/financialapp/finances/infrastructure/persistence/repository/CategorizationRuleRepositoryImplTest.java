package com.financialapp.finances.infrastructure.persistence.repository;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.RuleId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.rule.CategorizationRule;
import com.financialapp.finances.domain.model.rule.RuleMatchType;
import com.financialapp.finances.infrastructure.persistence.entity.CategorizationRuleJpaEntity;
import com.financialapp.finances.infrastructure.persistence.jpa.CategorizationRuleJpaRepository;
import com.financialapp.finances.infrastructure.persistence.mapper.CategorizationRulePersistenceMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CategorizationRuleRepositoryImplTest {

    private final CategorizationRuleJpaRepository jpa = mock(CategorizationRuleJpaRepository.class);
    private final CategorizationRulePersistenceMapper mapper = new CategorizationRulePersistenceMapper();
    private final CategorizationRuleRepositoryImpl repo = new CategorizationRuleRepositoryImpl(jpa, mapper);

    @Test
    void savePersistsAndReturnsReconstitutedRule() {
        CategorizationRule rule = CategorizationRule.create(new UserId(42L), RuleMatchType.CONTAINS, "YPF", new CategoryId(10L));
        CategorizationRuleJpaEntity entity = mapper.toEntity(rule);
        entity.setId(1L);

        when(jpa.save(any())).thenReturn(entity);

        CategorizationRule saved = repo.save(rule);

        assertThat(saved.id().value()).isEqualTo(1L);
        assertThat(saved.pattern()).isEqualTo("YPF");
        verify(jpa).save(any());
    }

    @Test
    void findByUserReturnsUserRules() {
        CategorizationRule rule = CategorizationRule.create(new UserId(42L), RuleMatchType.CONTAINS, "YPF", new CategoryId(10L));
        CategorizationRuleJpaEntity entity = mapper.toEntity(rule);
        entity.setId(1L);

        when(jpa.findByUserId(42L)).thenReturn(List.of(entity));

        List<CategorizationRule> result = repo.findByUser(new UserId(42L));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id().value()).isEqualTo(1L);
    }

    @Test
    void findByIdOwnedByScopesToUser() {
        CategorizationRule rule = CategorizationRule.create(new UserId(42L), RuleMatchType.CONTAINS, "YPF", new CategoryId(10L));
        CategorizationRuleJpaEntity entity = mapper.toEntity(rule);
        entity.setId(1L);

        when(jpa.findByIdAndUserId(1L, 42L)).thenReturn(Optional.of(entity));

        Optional<CategorizationRule> result = repo.findByIdOwnedBy(new RuleId(1L), new UserId(42L));

        assertThat(result).isPresent();
        assertThat(result.get().id().value()).isEqualTo(1L);
    }

    @Test
    void deleteRemovesEntity() {
        CategorizationRule rule = CategorizationRule.reconstitute(
                new RuleId(1L), new UserId(42L), RuleMatchType.CONTAINS, "YPF", new CategoryId(10L), 0, null);

        repo.delete(rule);

        verify(jpa).deleteById(1L);
    }
}
