package com.mahmoud.maalflow.modules.installments.payment.repo;

import com.mahmoud.maalflow.modules.installments.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Payment entity with comprehensive query methods.
 *
 * @author Mahmoud
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {

    /**
     * Find payment by idempotency key to prevent duplicate payments.
     */
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    /**
     * Find payments linked to a specific installment schedule.
     */
    List<Payment> findByInstallmentScheduleIdOrderByPaymentDateDesc(Long installmentScheduleId);

    /**
     * Check if payment exists by idempotency key.
     */
    boolean existsByIdempotencyKey(String idempotencyKey);

    /**
     * Find payments within a date range.
     */
    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate")
    Page<Payment> findByPaymentDateBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Find payments by agreed payment month.
     */
    Page<Payment> findByAgreedPaymentMonth(String agreedPaymentMonth, Pageable pageable);

    /**
     * Find payments by actual payment date.
     */
    Page<Payment> findByActualPaymentDate(LocalDate actualPaymentDate, Pageable pageable);

    // Statistical query methods for requirement 12: "حساب مجموع الأقساط المفترض ورودها شهريا والأقساط الواردة فعليا"

    /**
     * Sum payments by agreed payment month.
     */
    @Query("SELECT SUM(p.netAmount) FROM Payment p WHERE p.agreedPaymentMonth = :month")
    BigDecimal sumPaymentsByAgreedMonth(@Param("month") String month);

    /**
     * Sum overdue payments paid in a specific month.
     */
    @Query("SELECT SUM(p.netAmount) FROM Payment p WHERE p.agreedPaymentMonth = :month AND FUNCTION('YEAR_MONTH', p.actualPaymentDate) > FUNCTION('YEAR_MONTH', STR_TO_DATE(CONCAT(:month, '-01'), '%Y-%m-%d'))")
    BigDecimal sumOverduePaymentsForMonth(@Param("month") String month);

    /**
     * Sum early payments for a month.
     */
    @Query("SELECT SUM(p.netAmount) FROM Payment p WHERE p.agreedPaymentMonth = :month AND p.isEarlyPayment = true")
    BigDecimal sumEarlyPaymentsForMonth(@Param("month") String month);

    /**
     * Sum total discounts for a month.
     */
    @Query("SELECT SUM(p.discountAmount) FROM Payment p WHERE p.agreedPaymentMonth = :month")
    BigDecimal sumDiscountsForMonth(@Param("month") String month);

    /**
     * Count payments by agreed payment month.
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.agreedPaymentMonth = :month")
    int countPaymentsByAgreedMonth(@Param("month") String month);

    /**
     * Count early payments by month.
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.agreedPaymentMonth = :month AND p.isEarlyPayment = true")
    int countEarlyPaymentsByMonth(@Param("month") String month);

    /**
     * Get daily payment summaries for a date range.
     */
    @Query("SELECT p.actualPaymentDate as paymentDate, COUNT(p) as paymentCount, SUM(p.netAmount) as totalAmount " +
           "FROM Payment p WHERE p.actualPaymentDate BETWEEN :startDate AND :endDate " +
           "GROUP BY p.actualPaymentDate ORDER BY p.actualPaymentDate")
    List<Object[]> getDailyPaymentSummaries(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Sum payments for entire year.
     */
    @Query("SELECT SUM(p.netAmount) FROM Payment p WHERE YEAR(p.actualPaymentDate) = :year")
    BigDecimal sumPaymentsForYear(@Param("year") int year);

    /**
     * Sum discounts for entire year.
     */
    @Query("SELECT SUM(p.discountAmount) FROM Payment p WHERE YEAR(p.actualPaymentDate) = :year")
    BigDecimal sumDiscountsForYear(@Param("year") int year);

    /**
     * Count payments for entire year.
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE YEAR(p.actualPaymentDate) = :year")
    int countPaymentsForYear(@Param("year") int year);

    /**
     * Get customer payment performance.
     */
    @Query("SELECT c.id as customerId, c.name as customerName, COUNT(p) as paymentCount, SUM(p.netAmount) as totalPaid " +
           "FROM Payment p JOIN p.installmentSchedule.contract.customer c " +
           "WHERE p.agreedPaymentMonth = :month " +
           "GROUP BY c.id, c.name ORDER BY totalPaid DESC")
    List<Object[]> getCustomerPaymentPerformance(@Param("month") String month);

    /**
     * Find payments with specific criteria for advanced filtering.
     */
    @Query("SELECT p FROM Payment p WHERE " +
           "(:startDate IS NULL OR p.actualPaymentDate >= :startDate) AND " +
           "(:endDate IS NULL OR p.actualPaymentDate <= :endDate) AND " +
           "(:isEarlyPayment IS NULL OR p.isEarlyPayment = :isEarlyPayment)")
    Page<Payment> findWithFilters(@Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate,
                                 @Param("isEarlyPayment") Boolean isEarlyPayment,
                                 Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(p.netAmount), 0) FROM Payment p
            WHERE p.paymentDate BETWEEN :min AND :now
            """)
    BigDecimal getTotalPaymentsBetweenDates(@Param("min") LocalDateTime min, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.agreedPaymentMonth = :currentMonth")
    Long countByAgreedPaymentMonth(@Param("currentMonth") String currentMonth);
}
