package com.mahmoud.maalflow.modules.installments.capital.repo;

import com.mahmoud.maalflow.modules.installments.capital.entity.CapitalTransaction;
import com.mahmoud.maalflow.modules.installments.capital.enums.CapitalTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository for CapitalTransaction entity.
 * Updated for pooled capital model.
 *
 * @author Mahmoud
 */
@Repository
public interface CapitalTransactionRepository extends JpaRepository<CapitalTransaction, Long> {

    /**
     * Find transactions by capital pool ID.
     */
    List<CapitalTransaction> findByCapitalPoolIdOrderByTransactionDateDesc(Long capitalPoolId);

    /**
     * Find transactions by partner ID (for audit/reporting).
     */
    List<CapitalTransaction> findByPartnerIdOrderByTransactionDateDesc(Long partnerId);

    /**
     * Find transactions by contract ID.
     */
    List<CapitalTransaction> findByContractIdOrderByTransactionDateDesc(Long contractId);

    /**
     * Find transactions by transaction type.
     */
    List<CapitalTransaction> findByTransactionTypeOrderByTransactionDateDesc(CapitalTransactionType transactionType);

    /**
     * Find transactions within date range.
     */
    List<CapitalTransaction> findByTransactionDateBetweenOrderByTransactionDateDesc(
            LocalDate startDate, LocalDate endDate);

    /**
     * Find partner transactions by date range.
     */
    List<CapitalTransaction> findByPartnerIdAndTransactionDateBetweenOrderByTransactionDateDesc(
            Long partnerId, LocalDate startDate, LocalDate endDate);

    /**
     * Sum amounts by transaction type.
     */
    @Query("SELECT COALESCE(SUM(ct.amount), 0) FROM CapitalTransaction ct " +
            "WHERE ct.transactionType = :transactionType")
    BigDecimal sumAmountByTransactionType(@Param("transactionType") CapitalTransactionType transactionType);

    /**
     * Get monthly summary.
     */
    @Query("SELECT ct.transactionType, SUM(ct.amount) FROM CapitalTransaction ct " +
            "WHERE YEAR(ct.transactionDate) = :year AND MONTH(ct.transactionDate) = :month " +
            "GROUP BY ct.transactionType")
    List<Object[]> getMonthlySummary(@Param("year") int year, @Param("month") int month);

    /**
     * Get pool transactions for date range.
     */
    @Query("SELECT ct FROM CapitalTransaction ct WHERE ct.capitalPool.id = :poolId " +
            "AND ct.transactionDate BETWEEN :startDate AND :endDate " +
            "ORDER BY ct.transactionDate DESC")
    List<CapitalTransaction> findPoolTransactionsByDateRange(
            @Param("poolId") Long poolId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find transactions by reference type and ID.
     */
    List<CapitalTransaction> findByReferenceTypeAndReferenceIdOrderByTransactionDateDesc(
            String referenceType, Long referenceId);
}

