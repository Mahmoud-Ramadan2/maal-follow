package com.mahmoud.maalflow.modules.installments.contract.repo;

import com.mahmoud.maalflow.modules.installments.contract.entity.InstallmentSchedule;
import com.mahmoud.maalflow.modules.installments.contract.enums.PaymentStatus;
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
public interface InstallmentScheduleRepository extends JpaRepository<InstallmentSchedule, Long> {

    // Find all schedules for a specific contract
    List<InstallmentSchedule> findByContractIdOrderBySequenceNumberAsc(Long contractId);

    // Find schedules by status
    Page<InstallmentSchedule> findByStatus(PaymentStatus status, Pageable pageable);

    // Find overdue payments (due date passed and status is PENDING or LATE)
    @Query("""
        SELECT s FROM InstallmentSchedule s 
        WHERE s.dueDate < :currentDate 
        AND s.status IN ('PENDING', 'LATE')
        ORDER BY s.dueDate ASC
    """)
    List<InstallmentSchedule> findOverdueSchedules(@Param("currentDate") LocalDate currentDate);

    // Find schedules due within date range
    @Query("""
        SELECT s FROM InstallmentSchedule s
        WHERE s.dueDate BETWEEN :startDate AND :endDate
        AND s.status = :status
        ORDER BY s.dueDate ASC
   """)
    List<InstallmentSchedule> findSchedulesDueBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") PaymentStatus status
    );

    // Find schedules by profit month for profit distribution
    @Query("""
        SELECT s FROM InstallmentSchedule s 
        WHERE s.profitMonth = :profitMonth 
        AND s.status = 'PAID'
    """)
    List<InstallmentSchedule> findPaidSchedulesByProfitMonth(@Param("profitMonth") String profitMonth);

    // Find schedules by agreed payment day (for collection route optimization)
    @Query("""
        SELECT s FROM InstallmentSchedule s 
        JOIN s.contract c
        WHERE c.agreedPaymentDay = :paymentDay 
        AND s.status IN ('PENDING', 'LATE')
        ORDER BY c.customer.address
    """)
    List<InstallmentSchedule> findByAgreedPaymentDayAndPending(@Param("paymentDay") Integer paymentDay);

    // Find schedules by customer address for route planning
    @Query("""
        SELECT s FROM InstallmentSchedule s 
        JOIN s.contract c
        WHERE c.customer.address LIKE %:address% 
        AND s.status IN ('PENDING', 'LATE')
        ORDER BY s.dueDate ASC
    """)
    List<InstallmentSchedule> findPendingByCustomerAddress(@Param("address") String address);
    // Find schedules by customer Name
    @Query("""
        SELECT s FROM InstallmentSchedule s 
        JOIN s.contract c
        WHERE c.customer.name LIKE %:name% 
        AND s.status IN ('PENDING', 'LATE')
        ORDER BY s.dueDate ASC
    """)
    List<InstallmentSchedule> findPendingByCustomerName(@Param("name") String name);

    // Count pending schedules for a contract
    @Query("""
        SELECT COUNT(s) FROM InstallmentSchedule s 
        WHERE s.contract.id = :contractId 
        AND s.status IN ('PENDING', 'LATE', 'PARTIALLY_PAID')
    """)
    Long countPendingByContractId(@Param("contractId") Long contractId);

    // Get total expected vs actual payments for a month
    @Query("""
        SELECT COALESCE(SUM(s.amount), 0) FROM InstallmentSchedule s 
        WHERE s.profitMonth = :profitMonth
    """)
    BigDecimal getTotalExpectedForMonth(@Param("profitMonth") String profitMonth);

    @Query("""
        SELECT COALESCE(SUM(s.paidAmount), 0) FROM InstallmentSchedule s 
        WHERE s.profitMonth = :profitMonth 
        AND s.status = 'PAID'
    """)
    BigDecimal getTotalPaidForMonth(@Param("profitMonth") String profitMonth);

    @Query("""
        SELECT s FROM InstallmentSchedule s
        WHERE s.contract.id = :contractId
        AND s.status = 'PAID'
    """)
    List<InstallmentSchedule> findPaidByContractId(@Param("contractId") Long contractId);

    List<InstallmentSchedule> findByContractId(Long contractId);

    // Find schedules by profit month for profit distribution
    @Query("""
        SELECT s FROM InstallmentSchedule s 
        WHERE s.profitMonth = :profitMonth 
        AND s.status = 'COMPLETED'
        ORDER BY s.dueDate ASC
    """)
    List<InstallmentSchedule> findCompletedSchedulesByProfitMonth(@Param("profitMonth") String profitMonth);

    // Additional methods for payment statistics (requirement 12)

    /**
     * Calculate expected payments for a month range.
     */
    @Query("SELECT SUM(s.amount) FROM InstallmentSchedule s WHERE s.dueDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateExpectedPaymentsForMonth(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Calculate expected payments for a period.
     */
    @Query("SELECT SUM(s.amount) FROM InstallmentSchedule s WHERE s.dueDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateExpectedPaymentsForPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Calculate total overdue amount.
     */
    @Query("SELECT SUM(s.amount) FROM InstallmentSchedule s WHERE s.dueDate < :currentDate AND s.status IN ('PENDING', 'LATE')")
    BigDecimal calculateOverdueAmount(@Param("currentDate") LocalDate currentDate);

    /**
     * Count overdue installments.
     */
    @Query("SELECT COUNT(s) FROM InstallmentSchedule s WHERE s.dueDate < :currentDate AND s.status IN ('PENDING', 'LATE')")
    int countOverdueInstallments(@Param("currentDate") LocalDate currentDate);

    /**
     * Find unpaid installments due on specific date (for reminders).
     */
    @Query("SELECT s FROM InstallmentSchedule s WHERE s.dueDate = :dueDate AND s.status IN ('PENDING', 'LATE')")
    List<InstallmentSchedule> findUnpaidInstallmentsDueOnDate(@Param("dueDate") LocalDate dueDate);
}
