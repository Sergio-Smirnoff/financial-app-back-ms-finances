package com.financialapp.finances.repository;

import com.financialapp.finances.model.entity.CardExpenseInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CardExpenseInstallmentRepository extends JpaRepository<CardExpenseInstallment, Long> {

    List<CardExpenseInstallment> findByCardExpenseIdOrderByInstallmentNumberAsc(Long cardExpenseId);

    @Query("SELECT cei FROM CardExpenseInstallment cei " +
           "WHERE cei.cardExpense.id = :cardExpenseId AND cei.paid = false " +
           "ORDER BY cei.installmentNumber ASC")
    List<CardExpenseInstallment> findUnpaidByCardExpenseId(@Param("cardExpenseId") Long cardExpenseId);

    @Query("SELECT cei FROM CardExpenseInstallment cei JOIN FETCH cei.cardExpense ce " +
           "WHERE ce.userId = :userId AND cei.paid = false " +
           "AND cei.dueDate BETWEEN :from AND :to " +
           "AND (CAST(:currency AS string) IS NULL OR ce.currency = :currency) " +
           "ORDER BY cei.dueDate ASC")
    List<CardExpenseInstallment> findUpcomingUnpaidByUser(
            @Param("userId") Long userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("currency") String currency);

    @Query("SELECT COUNT(cei) FROM CardExpenseInstallment cei WHERE cei.cardExpense.id = :cardExpenseId " +
           "AND cei.installmentNumber < :installmentNumber AND cei.paid = false")
    int countUnpaidBefore(@Param("cardExpenseId") Long cardExpenseId, @Param("installmentNumber") int installmentNumber);

    @Query("SELECT COALESCE(SUM(cei.amount), 0) FROM CardExpenseInstallment cei " +
           "JOIN cei.cardExpense ce " +
           "WHERE ce.userId = :userId AND ce.currency = :currency " +
           "AND cei.paidDate BETWEEN :from AND :to AND cei.paid = true")
    BigDecimal sumPaidByUserAndCurrencyAndPaidDateRange(
            @Param("userId") Long userId,
            @Param("currency") String currency,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}
