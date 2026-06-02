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

    List<TransactionJpaEntity> findByUserIdAndDateBetweenOrderByDateDescIdDesc(
            Long userId, LocalDate from, LocalDate to);

    @Query("""
            SELECT t FROM TransactionJpaEntity t
            WHERE (t.fromCbu = :cbu OR t.toCbu = :cbu)
              AND (CAST(:from AS date) IS NULL OR t.date >= :from)
              AND (CAST(:to   AS date) IS NULL OR t.date <= :to)
            ORDER BY t.date DESC, t.id DESC
            """)
    List<TransactionJpaEntity> findByAccount(@Param("cbu") String cbu,
                                             @Param("from") LocalDate from,
                                             @Param("to") LocalDate to,
                                             Limit limit);

    @Query("""
            SELECT COUNT(t) > 0 FROM TransactionJpaEntity t
            WHERE t.userId = :userId
              AND t.fromCbu = :fromCbu
              AND t.toCbu = :toCbu
              AND t.amount = :amount
              AND t.currency = :currency
              AND t.date = :date
              AND t.description = :description
            """)
    boolean existsDuplicate(@Param("userId") Long userId,
                            @Param("fromCbu") String fromCbu,
                            @Param("toCbu") String toCbu,
                            @Param("amount") BigDecimal amount,
                            @Param("currency") String currency,
                            @Param("date") LocalDate date,
                            @Param("description") String description);
}
