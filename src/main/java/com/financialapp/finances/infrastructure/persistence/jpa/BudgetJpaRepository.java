package com.financialapp.finances.infrastructure.persistence.jpa;

import com.financialapp.finances.infrastructure.persistence.entity.BudgetJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BudgetJpaRepository extends JpaRepository<BudgetJpaEntity, Long> {

    List<BudgetJpaEntity> findByUserIdAndYearAndMonth(Long userId, Integer year, Integer month);

    Optional<BudgetJpaEntity> findByUserIdAndCategoryIdAndYearAndMonth(Long userId, Long categoryId, Integer year, Integer month);

    @Modifying
    @Query(value = """
            INSERT INTO finances.budgets (user_id, category_id, year, month, amount, currency, alert_threshold_pct, last_alerted_year, last_alerted_month, created_at, updated_at)
            VALUES (:userId, :categoryId, :year, :month, :amount, :currency, :alertThresholdPct, :lastAlertedYear, :lastAlertedMonth, NOW(), NOW())
            ON CONFLICT (user_id, category_id, year, month)
            DO UPDATE SET amount = EXCLUDED.amount, currency = EXCLUDED.currency, alert_threshold_pct = EXCLUDED.alert_threshold_pct,
                          last_alerted_year = EXCLUDED.last_alerted_year, last_alerted_month = EXCLUDED.last_alerted_month, updated_at = NOW()
            """, nativeQuery = true)
    void upsertBudget(@Param("userId") Long userId,
                     @Param("categoryId") Long categoryId,
                     @Param("year") Integer year,
                     @Param("month") Integer month,
                     @Param("amount") BigDecimal amount,
                     @Param("currency") String currency,
                     @Param("alertThresholdPct") BigDecimal alertThresholdPct,
                     @Param("lastAlertedYear") Integer lastAlertedYear,
                     @Param("lastAlertedMonth") Integer lastAlertedMonth);
}
