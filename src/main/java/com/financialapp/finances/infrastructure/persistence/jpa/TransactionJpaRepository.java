package com.financialapp.finances.infrastructure.persistence.jpa;

import com.financialapp.finances.infrastructure.persistence.entity.TransactionJpaEntity;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, Long> {

    Optional<TransactionJpaEntity> findByIdAndUserId(Long id, Long userId);

    List<TransactionJpaEntity> findByUserIdOrderByDateDescIdDesc(Long userId);

    @Query("""
            SELECT t FROM TransactionJpaEntity t
            WHERE (t.fromCbu = :cbu OR t.toCbu = :cbu)
              AND (:from IS NULL OR t.date >= :from)
              AND (:to   IS NULL OR t.date <= :to)
            ORDER BY t.date DESC, t.id DESC
            """)
    List<TransactionJpaEntity> findByAccount(@Param("cbu") String cbu,
                                             @Param("from") LocalDate from,
                                             @Param("to") LocalDate to,
                                             Limit limit);

    boolean existsByUserIdAndFromCbuAndToCbuAndAmountAndCurrencyAndDateAndDescription(
            Long userId, String fromCbu, String toCbu,
            BigDecimal amount, String currency, LocalDate date, String description);
}
