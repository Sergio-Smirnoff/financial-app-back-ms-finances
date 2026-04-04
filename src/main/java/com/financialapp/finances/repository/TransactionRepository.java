package com.financialapp.finances.repository;

import com.financialapp.finances.model.entity.Transaction;
import com.financialapp.finances.model.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId " +
           "AND (:type IS NULL OR t.type = :type) " +
           "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
           "AND (:currency IS NULL OR t.currency = :currency) " +
           "AND (CAST(:dateFrom AS date) IS NULL OR t.date >= :dateFrom) " +
           "AND (CAST(:dateTo AS date) IS NULL OR t.date <= :dateTo)")
    Page<Transaction> findFiltered(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("categoryId") Long categoryId,
            @Param("currency") String currency,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            Pageable pageable);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.userId = :userId AND t.type = :type AND t.currency = :currency " +
           "AND (CAST(:dateFrom AS date) IS NULL OR t.date >= :dateFrom) " +
           "AND (CAST(:dateTo AS date) IS NULL OR t.date <= :dateTo)")
    BigDecimal sumByTypeAndCurrency(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("currency") String currency,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo);
}
