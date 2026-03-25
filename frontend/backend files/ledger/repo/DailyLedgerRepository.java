package com.mahmoud.maalflow.modules.installments.ledger.repo;

import com.mahmoud.maalflow.modules.installments.ledger.entity.DailyLedger;
import com.mahmoud.maalflow.modules.installments.ledger.enums.LedgerSource;
import com.mahmoud.maalflow.modules.installments.ledger.enums.LedgerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for DailyLedger entity
 *
 * @author Mahmoud
 */
@Repository
public interface DailyLedgerRepository extends JpaRepository<DailyLedger, Long> {

    /**
     * Find ledger entry by idempotency key to prevent duplicates.
     */
    Optional<DailyLedger> findByIdempotencyKey(String idempotencyKey);

    /**
     * Check if ledger entry exists by idempotency key.
     */
    boolean existsByIdempotencyKey(String idempotencyKey);

    /**
     * Find ledger entries by date.
     */
    Page<DailyLedger> findByDate(LocalDate date, Pageable pageable);

    /**
     * Find ledger entries by type.
     */
    Page<DailyLedger> findByType(LedgerType type, Pageable pageable);

    /**
     * Find ledger entries by source.
     */
    Page<DailyLedger> findBySource(LedgerSource source, Pageable pageable);

    /**
     * Find ledger entries within a date range.
     */
    @Query("SELECT l FROM DailyLedger l WHERE l.date BETWEEN :startDate AND :endDate ORDER BY l.date DESC, l.createdAt DESC")
    Page<DailyLedger> findByDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    /**
     * Find ledger entries by partner.
     */
    Page<DailyLedger> findByPartnerId(Long partnerId, Pageable pageable);

    /**
     * Find ledger entries by user.
     */
    Page<DailyLedger> findByUserId(Long userId, Pageable pageable);

    /**
     * Calculate total income for a date range.
     */
    @Query("SELECT COALESCE(SUM(l.amount), 0) FROM DailyLedger l WHERE l.type = 'INCOME' AND l.date BETWEEN :startDate AND :endDate")
    BigDecimal getTotalIncomeForDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Calculate total expenses for a date range.
     */
    @Query("SELECT COALESCE(SUM(l.amount), 0) FROM DailyLedger l WHERE l.type = 'EXPENSE' AND l.date BETWEEN :startDate AND :endDate")
    BigDecimal getTotalExpensesForDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Count entries by type.
     */
    long countByType(LedgerType type);

    /**
     * Get daily summary for a date range.
     */
    @Query("""
            SELECT l.date,
           SUM(CASE WHEN l.type = 'INCOME' THEN l.amount ELSE 0 END),
           SUM(CASE WHEN l.type = 'EXPENSE' THEN l.amount ELSE 0 END),
           SUM(CASE WHEN l.type = 'INCOME' THEN 1 ELSE 0 END),
           SUM(CASE WHEN l.type = 'EXPENSE' THEN 1 ELSE 0 END)
           FROM DailyLedger l WHERE l.date BETWEEN :startDate AND :endDate
           GROUP BY l.date ORDER BY l.date DESC
           """)
    List<Object[]> getDailySummary(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get monthly summary.
     */
    @Query("SELECT FUNCTION('DATE_FORMAT', l.date, '%Y-%m'), " +
           "SUM(CASE WHEN l.type = 'INCOME' THEN l.amount ELSE 0 END), " +
           "SUM(CASE WHEN l.type = 'EXPENSE' THEN l.amount ELSE 0 END) " +
           "FROM DailyLedger l WHERE l.date BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DATE_FORMAT', l.date, '%Y-%m') " +
           "ORDER BY FUNCTION('DATE_FORMAT', l.date, '%Y-%m') DESC")
    List<Object[]> getMonthlySummary(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find by type and date range.
     */
    @Query("SELECT l FROM DailyLedger l WHERE l.type = :type AND l.date BETWEEN :startDate AND :endDate ORDER BY l.date DESC")
    Page<DailyLedger> findByTypeAndDateBetween(
            @Param("type") LedgerType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    /**
     * Search by description.
     */
    @Query("SELECT l FROM DailyLedger l WHERE LOWER(l.description) LIKE LOWER(CONCAT('%', :search, '%')) ORDER BY l.date DESC")
    Page<DailyLedger> searchByDescription(@Param("search") String search, Pageable pageable);
}

