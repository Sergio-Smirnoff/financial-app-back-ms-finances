package com.financialapp.finances.repository;

import com.financialapp.finances.model.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    @Query("SELECT l FROM Loan l WHERE l.userId = :userId " +
           "AND (:active IS NULL OR l.active = :active) " +
           "AND (:currency IS NULL OR l.currency = :currency) " +
           "ORDER BY l.createdAt DESC")
    List<Loan> findFiltered(
            @Param("userId") Long userId,
            @Param("active") Boolean active,
            @Param("currency") String currency);

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.userId = :userId AND l.active = true AND l.currency = :currency")
    int countActiveByUserIdAndCurrency(@Param("userId") Long userId, @Param("currency") String currency);

    @Query("SELECT COALESCE(SUM(l.installmentAmount * (l.totalInstallments - l.paidInstallments)), 0) " +
           "FROM Loan l WHERE l.userId = :userId AND l.active = true AND l.currency = :currency")
    BigDecimal sumRemainingDebtByUserIdAndCurrency(@Param("userId") Long userId, @Param("currency") String currency);
}
