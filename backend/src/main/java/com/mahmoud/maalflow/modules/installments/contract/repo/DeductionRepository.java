package com.mahmoud.maalflow.modules.installments.contract.repo;

import com.mahmoud.maalflow.modules.installments.contract.entity.Deduction;
import com.mahmoud.maalflow.modules.installments.contract.enums.DeductionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Deduction entity.
 *
 * @author Mahmoud
 */
@Repository
public interface DeductionRepository extends JpaRepository<Deduction, Long> {

    List<Deduction> findByContractId(Long contractId);

    List<Deduction> findByInstallmentScheduleId(Long scheduleId);

    List<Deduction> findByMonth(String month);

    List<Deduction> findByDeductionType(DeductionType type);

    List<Deduction> findByDeductionDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(d.amount) FROM Deduction d WHERE d.month = :month AND d.deductionType = :type")
    BigDecimal sumByMonthAndType(@Param("month") String month, @Param("type") DeductionType type);

    @Query("SELECT SUM(d.amount) FROM Deduction d WHERE d.contract.id = :contractId AND d.deductionType = :type")
    BigDecimal sumByContractAndType(@Param("contractId") Long contractId, @Param("type") DeductionType type);

    @Query("SELECT SUM(d.amount) FROM Deduction d WHERE d.month = :month")
    BigDecimal sumByMonth(@Param("month") String month);

    @Query("SELECT SUM(d.amount) FROM Deduction d WHERE d.deductionType = :type")
    BigDecimal sumByType(@Param("type") DeductionType type);
}

