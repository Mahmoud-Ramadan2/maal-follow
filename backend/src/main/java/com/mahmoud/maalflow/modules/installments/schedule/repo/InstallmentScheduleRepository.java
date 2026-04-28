package com.mahmoud.maalflow.modules.installments.schedule.repo;

import com.mahmoud.maalflow.modules.installments.schedule.entity.InstallmentSchedule;
import com.mahmoud.maalflow.modules.installments.contract.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface InstallmentScheduleRepository extends JpaRepository<InstallmentSchedule, Long> {

    // Find all schedules for a specific contract
    List<InstallmentSchedule> findByContractIdOrderBySequenceNumberAsc(Long contractId);

    Page<InstallmentSchedule> findByContractIdOrderBySequenceNumberAsc(Pageable pageable, Long contractId);

    // Find schedules by status
    Page<InstallmentSchedule> findByStatus(PaymentStatus status, Pageable pageable);

    @Query(value = """
        SELECT s FROM InstallmentSchedule s
        JOIN s.contract c
        JOIN c.customer cu
        WHERE (:contractId IS NULL OR c.id = :contractId)
          AND (:status IS NULL OR s.status = :status)
          AND (:paymentDay IS NULL OR c.agreedPaymentDay = :paymentDay)
          AND (:name IS NULL OR TRIM(:name) = '' OR LOWER(cu.name) LIKE LOWER(CONCAT('%', :name, '%')))
          AND (:startDate IS NULL OR s.dueDate >= :startDate)
          AND (:endDate IS NULL OR s.dueDate <= :endDate)
          AND (
                (:overdueOnly = FALSE AND :dueSoonDate IS NULL)
                OR (:overdueOnly = TRUE AND s.dueDate < :today AND s.status IN ('PARTIALLY_PAID', 'PENDING', 'LATE'))
                OR (:dueSoonDate IS NOT NULL AND s.dueDate BETWEEN :today AND :dueSoonDate AND s.status  IN ('PARTIALLY_PAID', 'PENDING', 'LATE'))
                )
        """)
    Page<InstallmentSchedule> searchSchedules(
            @Param("contractId") Long contractId,
            @Param("status") PaymentStatus status,
            @Param("name") String name,
            @Param("paymentDay") Integer paymentDay,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("overdueOnly") boolean overdueOnly,
            @Param("today") LocalDate today,
            @Param("dueSoonDate") LocalDate dueSoonDate,
            Pageable pageable
    );

    // Find overdue payments (due date passed and status is PARTIALLY_PAID,  PENDING or LATE)
    @Query("""
        SELECT s FROM InstallmentSchedule s 
        WHERE s.dueDate < :currentDate 
        AND s.status IN ('PARTIALLY_PAID', 'PENDING', 'LATE')
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

    @Query("""
            SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM InstallmentSchedule s
                        WHERE s.contract.id = :contractId AND s.status IN ( 'PAID', 'PARTIALLY_PAID')
            """)
    boolean existsPaidByContractId(Long contractId);
// this faster as it stop when find any matches also it type safety
    boolean existsByContractIdAndStatusIn(Long contractId, Collection<PaymentStatus> status);


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
