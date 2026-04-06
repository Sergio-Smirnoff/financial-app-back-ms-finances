package com.financialapp.finances.repository;

import com.financialapp.finances.model.entity.LoanInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanInstallmentRepository extends JpaRepository<LoanInstallment, Long> {

    List<LoanInstallment> findByLoanIdOrderByInstallmentNumberAsc(Long loanId);

    Optional<LoanInstallment> findByLoanIdAndInstallmentNumber(Long loanId, int installmentNumber);

    @Query("SELECT li FROM LoanInstallment li " +
           "WHERE li.paid = false AND li.dueDate BETWEEN :from AND :to")
    List<LoanInstallment> findUpcomingUnpaid(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("SELECT li FROM LoanInstallment li " +
           "WHERE li.loan.id = :loanId AND li.paid = false " +
           "ORDER BY li.installmentNumber ASC")
    List<LoanInstallment> findUnpaidByLoanId(@Param("loanId") Long loanId);

    @Query("SELECT li FROM LoanInstallment li JOIN FETCH li.loan l " +
           "WHERE l.userId = :userId AND li.paid = false " +
           "AND li.dueDate BETWEEN :from AND :to " +
           "AND (CAST(:currency AS string) IS NULL OR l.currency = :currency) " +
           "ORDER BY li.dueDate ASC")
    List<LoanInstallment> findUpcomingUnpaidByUser(
            @Param("userId") Long userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("currency") String currency);

    @Query("SELECT COALESCE(SUM(li.amount), 0) FROM LoanInstallment li " +
           "JOIN li.loan l " +
           "WHERE l.userId = :userId AND l.currency = :currency " +
           "AND li.paidDate BETWEEN :from AND :to AND li.paid = true")
    BigDecimal sumPaidByUserAndCurrencyAndPaidDateRange(
            @Param("userId") Long userId,
            @Param("currency") String currency,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}
