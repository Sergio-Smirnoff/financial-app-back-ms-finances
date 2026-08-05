package com.financialapp.finances.infrastructure.persistence.repository;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.budget.Budget;
import com.financialapp.finances.domain.model.budget.BudgetPeriod;
import com.financialapp.finances.domain.repository.BudgetRepository;
import com.financialapp.finances.infrastructure.persistence.entity.BudgetJpaEntity;
import com.financialapp.finances.infrastructure.persistence.jpa.BudgetJpaRepository;
import com.financialapp.finances.infrastructure.persistence.mapper.BudgetPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BudgetRepositoryImpl implements BudgetRepository {

    private final BudgetJpaRepository jpaRepository;
    private final BudgetPersistenceMapper mapper;

    public BudgetRepositoryImpl(BudgetJpaRepository jpaRepository, BudgetPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Budget upsert(Budget budget) {
        BudgetJpaEntity entity = mapper.toEntity(budget);
        jpaRepository.upsertBudget(
                entity.getUserId(),
                entity.getCategoryId(),
                entity.getYear(),
                entity.getMonth(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getAlertThresholdPct(),
                entity.getLastAlertedYear(),
                entity.getLastAlertedMonth()
        );
        BudgetJpaEntity saved = jpaRepository.findByUserIdAndCategoryIdAndYearAndMonth(
                entity.getUserId(), entity.getCategoryId(), entity.getYear(), entity.getMonth()
        ).orElseThrow();
        return mapper.toDomain(saved);
    }

    @Override
    public List<Budget> findByUserAndPeriod(UserId userId, BudgetPeriod period) {
        return jpaRepository.findByUserIdAndYearAndMonth(userId.value(), period.year(), period.month())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Budget> findByPeriod(BudgetPeriod period) {
        return jpaRepository.findByYearAndMonth(period.year(), period.month())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Budget> findByUserCategoryAndPeriod(UserId userId, CategoryId categoryId, BudgetPeriod period) {
        return jpaRepository.findByUserIdAndCategoryIdAndYearAndMonth(userId.value(), categoryId.value(), period.year(), period.month())
                .map(mapper::toDomain);
    }
}
