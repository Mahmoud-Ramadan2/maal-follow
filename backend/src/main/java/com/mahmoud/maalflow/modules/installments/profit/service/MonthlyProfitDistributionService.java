package com.mahmoud.maalflow.modules.installments.profit.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.modules.installments.partner.enums.ProfitStatus;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerMonthlyProfitRepository;
import com.mahmoud.maalflow.modules.installments.partner.service.PartnerProfitCalculationService;
import com.mahmoud.maalflow.modules.installments.profit.dto.MonthlyProfitDistributionRequest;
import com.mahmoud.maalflow.modules.installments.profit.entity.MonthlyProfitDistribution;
import com.mahmoud.maalflow.modules.installments.profit.enums.ProfitDistributionStatus;
import com.mahmoud.maalflow.modules.installments.profit.repo.MonthlyProfitDistributionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

import static com.mahmoud.maalflow.modules.shared.constants.AppConstants.MONTH_FORMAT;

/**
 * Service for managing monthly profit distribution calculations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonthlyProfitDistributionService {

    private final MonthlyProfitDistributionRepository repository;
    private final PartnerProfitCalculationService partnerProfitCalculationService;
    private final PartnerMonthlyProfitRepository partnerMonthlyProfitRepository;

    @Transactional
    public MonthlyProfitDistribution createDistribution(MonthlyProfitDistributionRequest request) {
        validateMonthYear(request.getMonthYear());

        // Validate month-year uniqueness
        if (repository.existsByMonthYear(request.getMonthYear())) {
            throw new BusinessException("profit.distribution.exists");
        }

        MonthlyProfitDistribution distribution = toEntity(request);

        // Calculate deductions and distributable profit
        calculateDistribution(distribution);

        return repository.save(distribution);
    }

    @Transactional(readOnly = true)
    public MonthlyProfitDistribution getDistributionById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("profit.distribution.notFound"));
    }

    @Transactional(readOnly = true)
    public MonthlyProfitDistribution getDistributionByIdForUpdate(Long id) {
        return repository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException("profit.distribution.notFound"));
    }

    @Transactional(readOnly = true)
    public MonthlyProfitDistribution getDistributionByMonth(String monthYear) {
        validateMonthYear(monthYear);
        return repository.findByMonthYear(monthYear)
                .orElseThrow(() -> new BusinessException("profit.distribution.notFound"));
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
        validateMonthYear(startMonth);
        validateMonthYear(endMonth);
        if (startMonth.compareTo(endMonth) > 0) {
            throw new BusinessException("profit.distribution.invalidMonthRange");
        }
        return repository.findByMonthYearBetweenOrderByMonthYearDesc(startMonth, endMonth);
    }

    @Transactional
    public MonthlyProfitDistribution calculateProfit(Long id) {
        MonthlyProfitDistribution distribution = getDistributionByIdForUpdate(id);

        validateCanCalculate(distribution);

        calculateDistribution(distribution);
        distribution.setStatus(ProfitDistributionStatus.CALCULATED);

        return repository.save(distribution);
    }

    // TODO when this called? last month or  10 of month

    // Run every month on the 5th at 11:59 PM to distribute profit for the previous month
    @Scheduled(cron = "0 59 23 5 * ?")
    @Transactional
    public void autoDistributeProfit(){
        log.info("Starting scheduled task to auto-distribute profit for previous month at {}", LocalDateTime.now());
        // get Perviews month
        YearMonth perviewsMonth = YearMonth.now().minusMonths(1);
        String monthYear = perviewsMonth.format(MONTH_FORMAT);

        // get distribute by month
        MonthlyProfitDistribution distribution = repository.findByMonthYear(monthYear)
                .orElseThrow(() -> new BusinessException("profit.distribution.notFound"));
        distributeProfit(distribution.getId());

    }
    @Transactional
    public MonthlyProfitDistribution distributeProfit(Long id) {

        log.info("Starting profit distribution for distribution ID: {}", id);
        MonthlyProfitDistribution distribution = getDistributionByIdForUpdate(id);

        if (distribution.getStatus() != ProfitDistributionStatus.DISTRIBUTED) {
            throw new BusinessException("profit.distribution.alreadyDistributed");
        }

        // Only run this method from the end of distribution.getMonthYear() to 5th of next month
        YearMonth distributionMonthYear =  YearMonth.parse(distribution.getMonthYear());
        LocalDateTime endOfMonth =  distributionMonthYear.atEndOfMonth().atTime(20, 59, 59);
        LocalDateTime endOfAllowedDistributionTime = distributionMonthYear.plusMonths(1).atDay(5).atTime(23, 59, 59);
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(endOfMonth) || now.isAfter(endOfAllowedDistributionTime)) {
            throw new BusinessException("profit.distribution.distributionDate.invalid ");
        }


        boolean alreadyGenerated = !partnerMonthlyProfitRepository
                .findByProfitDistributionId(distribution.getId())
                .isEmpty();
        if (alreadyGenerated) {
            throw new BusinessException("profit.distribution.alreadyGenerated");
        }

        // calculate and distribute monthly profit for each partner depending on effective investments
        // effectiveInvestments = all investments before (last minute  distribution month - 2) - all withdrawals before last minute  distribution month
        // ex:  for distribution month 2024-04, we consider all investments before 2026-02-28 23:59:59
        // and all withdrawals before 2024-04-30 23:59:59
        partnerProfitCalculationService.calculateAndDistributeMonthlyProfits(distribution);

        distribution.setStatus(ProfitDistributionStatus.DISTRIBUTED);
        return repository.save(distribution);
    }

    // undo distribtion by set distribution.setStatus calculated and delete all partner monthly profits for this distribution
    // only if partner monthly profits status is calculated not paid
    @Transactional
    public MonthlyProfitDistribution undoDistribution(Long id) {
        MonthlyProfitDistribution distribution = getDistributionByIdForUpdate(id);

        if (distribution.getStatus() != ProfitDistributionStatus.DISTRIBUTED) {
            throw new BusinessException("profit.distribution.notDistributed");
        }

        // any status other than calculated means profit already paid or used for at least one partner, so we can't undo distribution
        boolean hasPaidProfits = partnerMonthlyProfitRepository.existsByProfitDistributionIdAndStatusNot(distribution.getId(), ProfitStatus.CALCULATED);
        if (hasPaidProfits) {
            throw new BusinessException("profit.distribution.undoNotAllowed");
        }

        //  Only allow undo distribution before the end of distribution.getMonthYear() to 5th of next month
        YearMonth distributionMonthYear =  YearMonth.parse(distribution.getMonthYear());
        LocalDateTime endOfAllowedUndoTime = distributionMonthYear.plusMonths(1).atDay(5).atTime(10, 59, 59);
        LocalDateTime now = LocalDateTime.now();
        if ( now.isAfter(endOfAllowedUndoTime)) {
            throw new BusinessException("profit.distribution.undoDate.invalid");
        }

        partnerMonthlyProfitRepository.deleteByProfitDistributionId(distribution.getId());

        distribution.setStatus(ProfitDistributionStatus.CALCULATED);
        return repository.save(distribution);
    }

    @Transactional
    public MonthlyProfitDistribution lockDistribution(Long id) {
        MonthlyProfitDistribution distribution = getDistributionByIdForUpdate(id);

        if (distribution.getStatus() != ProfitDistributionStatus.DISTRIBUTED) {
            throw new BusinessException("profit.distribution.notDistributed");
        }

        distribution.setStatus(ProfitDistributionStatus.LOCKED);
        return repository.save(distribution);
    }

    @Transactional
    public MonthlyProfitDistribution updateDistribution(Long id, MonthlyProfitDistributionRequest details) {
        MonthlyProfitDistribution distribution = getDistributionByIdForUpdate(id);

        if (distribution.getStatus() == ProfitDistributionStatus.LOCKED) {
            throw new BusinessException("profit.distribution.locked");
        }

        validateMonthYear(details.getMonthYear());

        if (!distribution.getMonthYear().equals(details.getMonthYear())) {
            throw new BusinessException("profit.distribution.monthImmutable");
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
        MonthlyProfitDistribution distribution = getDistributionByIdForUpdate(id);

        if (distribution.getStatus() == ProfitDistributionStatus.LOCKED) {
            throw new BusinessException("profit.distribution.locked");
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
        BigDecimal managementPercentage = distribution.getManagementFeePercentage();
        BigDecimal zakatPercentage = distribution.getZakatPercentage();

        if (totalProfit == null || totalProfit.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("profit.distribution.totalProfit.invalid");
        }
        if (managementPercentage == null
                || managementPercentage.compareTo(BigDecimal.ZERO) < 0
                || managementPercentage.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessException("profit.distribution.managementFee.invalid");
        }
        if (zakatPercentage == null
                || zakatPercentage.compareTo(BigDecimal.ZERO) < 0
                || zakatPercentage.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessException("profit.distribution.zakat.invalid");
        }
        if (managementPercentage.add(zakatPercentage).compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessException("profit.distribution.percentages.sum.invalid");
        }

        // Calculate management fee
        BigDecimal managementFee = totalProfit
                .multiply(managementPercentage)
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
        if (distributableProfit.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("profit.distribution.distributable.invalid");
        }
        log.info("Calculated distribution for month {}: totalProfit={}, managementFee={}, zakat={}, distributableProfit={}",
                distribution.getMonthYear(), totalProfit, managementFee, zakat, distributableProfit);

        distribution.setDistributableProfit(distributableProfit);

    }

    private void validateMonthYear(String monthYear) {
        if (monthYear == null || monthYear.isBlank()) {
            throw new BusinessException("profit.distribution.monthYear.invalid");
        }

        try {
            YearMonth.parse(monthYear, MONTH_FORMAT);
        } catch (DateTimeParseException ex) {
            throw new BusinessException("profit.distribution.monthYear.invalid");
        }
    }

    private void validateCanCalculate(MonthlyProfitDistribution distribution) {
        if (distribution.getStatus() == ProfitDistributionStatus.LOCKED) {
            throw new BusinessException("profit.distribution.locked");
        }
        if (distribution.getStatus() == ProfitDistributionStatus.DISTRIBUTED) {
            throw new BusinessException("profit.distribution.recalculateAfterDistribution.notAllowed");
        }
    }

    private MonthlyProfitDistribution toEntity(MonthlyProfitDistributionRequest request) {
        MonthlyProfitDistribution distribution = new MonthlyProfitDistribution();
        distribution.setMonthYear(request.getMonthYear());
        distribution.setTotalProfit(request.getTotalProfit());
        distribution.setManagementFeePercentage(request.getManagementFeePercentage());
        distribution.setZakatPercentage(request.getZakatPercentage());
        distribution.setCalculationNotes(request.getCalculationNotes());
        distribution.setStatus(ProfitDistributionStatus.PENDING);
        return distribution;
    }
}

