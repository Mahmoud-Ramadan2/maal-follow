package com.mahmoud.maalflow.modules.installments.profit.repo;

import com.mahmoud.maalflow.modules.installments.profit.entity.MonthlyProfitDistribution;
import com.mahmoud.maalflow.modules.installments.profit.enums.ProfitDistributionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for MonthlyProfitDistribution entity.
 */
@Repository
public interface MonthlyProfitDistributionRepository extends JpaRepository<MonthlyProfitDistribution, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT mpd FROM MonthlyProfitDistribution mpd WHERE mpd.id = :id")
    Optional<MonthlyProfitDistribution> findByIdForUpdate(Long id);

    Optional<MonthlyProfitDistribution> findByMonthYear(String monthYear);

    List<MonthlyProfitDistribution> findByStatus(ProfitDistributionStatus status);

    List<MonthlyProfitDistribution> findByMonthYearBetweenOrderByMonthYearDesc(String startMonth, String endMonth);

    @Query("SELECT mpd FROM MonthlyProfitDistribution mpd WHERE mpd.status = :status ORDER BY mpd.monthYear DESC")
    List<MonthlyProfitDistribution> findByStatusOrderByMonthYearDesc(ProfitDistributionStatus status);

    boolean existsByMonthYear(String monthYear);

    @Query("SELECT mpd FROM MonthlyProfitDistribution mpd ORDER BY mpd.monthYear DESC")
    List<MonthlyProfitDistribution> findAllOrderByMonthYearDesc();
}

