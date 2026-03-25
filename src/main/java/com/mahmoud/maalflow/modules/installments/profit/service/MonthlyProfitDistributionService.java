package com.mahmoud.maalflow.modules.installments.profit.service;

import com.mahmoud.maalflow.modules.installments.profit.entity.MonthlyProfitDistribution;
import com.mahmoud.maalflow.modules.installments.profit.enums.ProfitDistributionStatus;
import com.mahmoud.maalflow.modules.installments.profit.repo.MonthlyProfitDistributionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Service for managing monthly profit distribution calculations.
 */
@Service
@RequiredArgsConstructor
public class MonthlyProfitDistributionService {

    private final MonthlyProfitDistributionRepository repository;

    @Transactional
    public MonthlyProfitDistribution createDistribution(MonthlyProfitDistribution distribution) {
        // Validate month-year uniqueness
        if (repository.existsByMonthYear(distribution.getMonthYear())) {
            throw new RuntimeException("Distribution already exists for month: " + distribution.getMonthYear());
        }

        // Calculate deductions and distributable profit
        calculateDistribution(distribution);

        return repository.save(distribution);
    }

    @Transactional(readOnly = true)
    public MonthlyProfitDistribution getDistributionById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profit distribution not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public MonthlyProfitDistribution getDistributionByMonth(String monthYear) {
        return repository.findByMonthYear(monthYear)
                .orElseThrow(() -> new RuntimeException("Profit distribution not found for month: " + monthYear));
    }

    @Transactional(readOnly = true)
    public List<MonthlyProfitDistribution> getAllDistributions() {
        return repository.findAllOrderByMonthYearDesc();
    }

    @Transactional(readOnly = true)
    public List<MonthlyProfitDistribution> getDistributionsByStatus(ProfitDistributionStatus status) {
        return repository.findByStatusOrderByMonthYearDesc(status);
    }

    @Transactional(readOnly = true)
    public List<MonthlyProfitDistribution> getDistributionsByDateRange(String startMonth, String endMonth) {
        return repository.findByMonthYearBetweenOrderByMonthYearDesc(startMonth, endMonth);
    }

    @Transactional
    public MonthlyProfitDistribution calculateProfit(Long id) {
        MonthlyProfitDistribution distribution = getDistributionById(id);

        if (distribution.getStatus() == ProfitDistributionStatus.LOCKED) {
            throw new RuntimeException("Cannot recalculate locked distribution");
        }

        calculateDistribution(distribution);
        distribution.setStatus(ProfitDistributionStatus.CALCULATED);

        return repository.save(distribution);
    }

    @Transactional
    public MonthlyProfitDistribution distributeProfit(Long id) {
        MonthlyProfitDistribution distribution = getDistributionById(id);

        if (distribution.getStatus() != ProfitDistributionStatus.CALCULATED) {
            throw new RuntimeException("Distribution must be calculated before distributing");
        }

        // TODO: Create PartnerMonthlyProfit records for each partner
        // This will be implemented when partner profit calculation is ready

        distribution.setStatus(ProfitDistributionStatus.DISTRIBUTED);
        return repository.save(distribution);
    }

    @Transactional
    public MonthlyProfitDistribution lockDistribution(Long id) {
        MonthlyProfitDistribution distribution = getDistributionById(id);

        if (distribution.getStatus() != ProfitDistributionStatus.DISTRIBUTED) {
            throw new RuntimeException("Distribution must be distributed before locking");
        }

        distribution.setStatus(ProfitDistributionStatus.LOCKED);
        return repository.save(distribution);
    }

    @Transactional
    public MonthlyProfitDistribution updateDistribution(Long id, MonthlyProfitDistribution details) {
        MonthlyProfitDistribution distribution = getDistributionById(id);

        if (distribution.getStatus() == ProfitDistributionStatus.LOCKED) {
            throw new RuntimeException("Cannot update locked distribution");
        }

        // Update fields
        distribution.setTotalProfit(details.getTotalProfit());
        distribution.setManagementFeePercentage(details.getManagementFeePercentage());
        distribution.setZakatPercentage(details.getZakatPercentage());
        distribution.setCalculationNotes(details.getCalculationNotes());

        // Recalculate
        calculateDistribution(distribution);

        return repository.save(distribution);
    }

    @Transactional
    public void deleteDistribution(Long id) {
        MonthlyProfitDistribution distribution = getDistributionById(id);

        if (distribution.getStatus() == ProfitDistributionStatus.LOCKED) {
            throw new RuntimeException("Cannot delete locked distribution");
        }

        repository.delete(distribution);
    }

    /**
     * Calculate deductions and distributable profit.
     * Formula:
     * - Management Fee = Total Profit * Management Fee %
     * - Zakat = Total Profit * Zakat %
     * - Distributable Profit = Total Profit - Management Fee - Zakat
     */
    private void calculateDistribution(MonthlyProfitDistribution distribution) {
        BigDecimal totalProfit = distribution.getTotalProfit();
        BigDecimal managementFeePercentage = distribution.getManagementFeePercentage();
        BigDecimal zakatPercentage = distribution.getZakatPercentage();

        // Calculate management fee
        BigDecimal managementFee = totalProfit
                .multiply(managementFeePercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        distribution.setManagementFeeAmount(managementFee);

        // Calculate zakat
        BigDecimal zakat = totalProfit
                .multiply(zakatPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        distribution.setZakatAmount(zakat);

        // Calculate distributable profit
        BigDecimal distributableProfit = totalProfit
                .subtract(managementFee)
                .subtract(zakat);
        distribution.setDistributableProfit(distributableProfit);

        // Note: Owner profit and partners' profit will be calculated
        // when partner profit calculation service is implemented
    }
}

