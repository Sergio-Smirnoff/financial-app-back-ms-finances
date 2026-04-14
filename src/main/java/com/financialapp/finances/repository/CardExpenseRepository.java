package com.financialapp.finances.repository;

import com.financialapp.finances.model.entity.CardExpense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CardExpenseRepository extends JpaRepository<CardExpense, Long> {

    @Query("SELECT ce FROM CardExpense ce WHERE ce.userId = :userId " +
           "AND (:active IS NULL OR ce.active = :active) " +
           "AND (:cardId IS NULL OR ce.cardId = :cardId) " +
           "AND (:currency IS NULL OR ce.currency = :currency) " +
           "ORDER BY ce.createdAt DESC")
    Page<CardExpense> findFiltered(
            @Param("userId") Long userId,
            @Param("active") Boolean active,
            @Param("cardId") Long cardId,
            @Param("currency") String currency,
            Pageable pageable);

    @Query("SELECT ce FROM CardExpense ce " +
           "WHERE ce.active = true AND ce.nextDueDate BETWEEN :from AND :to")
    List<CardExpense> findActiveWithUpcomingDueDate(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("SELECT COUNT(ce) FROM CardExpense ce " +
           "WHERE ce.userId = :userId AND ce.active = true AND ce.currency = :currency")
    int countActiveByUserIdAndCurrency(@Param("userId") Long userId, @Param("currency") String currency);

    @Query("SELECT COALESCE(SUM(ce.installmentAmount * ce.remainingInstallments), 0) " +
           "FROM CardExpense ce WHERE ce.userId = :userId AND ce.active = true AND ce.currency = :currency")
    BigDecimal sumRemainingDebtByUserIdAndCurrency(@Param("userId") Long userId, @Param("currency") String currency);

    @Query("SELECT ce FROM CardExpense ce " +
           "WHERE ce.userId = :userId AND ce.active = true " +
           "AND ce.nextDueDate BETWEEN :from AND :to " +
           "AND (CAST(:currency AS string) IS NULL OR ce.currency = :currency) " +
           "ORDER BY ce.nextDueDate ASC")
    List<CardExpense> findActiveWithUpcomingDueDateByUser(
            @Param("userId") Long userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("currency") String currency);
}
