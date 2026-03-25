package com.mahmoud.maalflow.modules.installments.partner.service;

import com.mahmoud.maalflow.modules.installments.partner.entity.*;
import com.mahmoud.maalflow.modules.installments.partner.enums.PartnerStatus;
import com.mahmoud.maalflow.modules.installments.partner.enums.ProfitStatus;
import com.mahmoud.maalflow.modules.installments.partner.enums.WithdrawalType;
import com.mahmoud.maalflow.modules.installments.partner.repo.*;
import com.mahmoud.maalflow.modules.installments.profit.entity.MonthlyProfitDistribution;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Advanced partner profit calculation service.
 * Handles complex profit distribution logic according to business requirements.
 */
@Service
@AllArgsConstructor
@Slf4j
public class PartnerProfitCalculationService {

    private final PartnerRepository partnerRepository;
    private final PartnerMonthlyProfitRepository monthlyProfitRepository;
    private final PartnerWithdrawalRepository withdrawalRepository;
    private final PartnerProfitCalculationConfigRepository configRepository;

    /**
     * Calculate monthly profits for all active partners according to business rules:
     * 1. New partners start earning after 2 months
     * 2. Management fee and zakat are deducted before distribution
     * 3. Profits are calculated based on investment percentage
     * 4. Withdrawals affect future calculations
     */
    @Transactional
    public void calculateMonthlyProfits(MonthlyProfitDistribution profitDistribution) {
        log.info("Starting monthly profit calculation for period: {}",
                profitDistribution.getMonthYear());

        // Get active configuration
        PartnerProfitCalculationConfig config = getActiveConfig();

        // Get all active partners
        List<Partner> activePartners = partnerRepository.findByStatusAndProfitSharingActive(
                PartnerStatus.ACTIVE, true);

        BigDecimal totalDistributableProfit = calculateDistributableProfit(
                profitDistribution.getTotalProfit(), config);

        for (Partner partner : activePartners) {
            if (isEligibleForProfit(partner, profitDistribution.getMonthYear(), config)) {
                calculatePartnerMonthlyProfit(partner, profitDistribution,
                        totalDistributableProfit, config);
            }
        }

        log.info("Completed monthly profit calculation for {} partners", activePartners.size());
    }

    /**
     * Calculate distributable profit after deducting management fee and zakat.
     */
    private BigDecimal calculateDistributableProfit(BigDecimal totalProfit,
                                                  PartnerProfitCalculationConfig config) {
        // Deduct management fee
        BigDecimal managementFee = totalProfit
                .multiply(config.getManagementFeePercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Deduct zakat
        BigDecimal zakatAmount = totalProfit
                .multiply(config.getZakatPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal distributableProfit = totalProfit
                .subtract(managementFee)
                .subtract(zakatAmount);

        log.debug("Total profit: {}, Management fee: {}, Zakat: {}, Distributable: {}",
                totalProfit, managementFee, zakatAmount, distributableProfit);

        return distributableProfit;
    }

    /**
     * Check if partner is eligible for profit based on 2-month rule.
     */
    private boolean isEligibleForProfit(Partner partner, String distributionMonth,
                                      PartnerProfitCalculationConfig config) {
        if (partner.getProfitCalculationStartMonth() == null) {
            log.warn("Partner {} has no profit calculation start month set", partner.getId());
            return false;
        }

        LocalDate startDate = LocalDate.parse(partner.getProfitCalculationStartMonth() + "-01");
        LocalDate distributionDate = LocalDate.parse(distributionMonth + "-01");

        // Check if enough months have passed
        long monthsElapsed = startDate.until(distributionDate).toTotalMonths();
        boolean eligible = monthsElapsed >= config.getNewPartnerDelayMonths();

        log.debug("Partner {} eligibility: start={}, distribution={}, months elapsed={}, eligible={}",
                partner.getId(), startDate, distributionDate, monthsElapsed, eligible);

        return eligible;
    }

    /**
     * Calculate individual partner's monthly profit.
     */
    private void calculatePartnerMonthlyProfit(Partner partner,
                                             MonthlyProfitDistribution profitDistribution,
                                             BigDecimal totalDistributableProfit,
                                             PartnerProfitCalculationConfig config) {

        BigDecimal effectiveInvestment = calculateEffectiveInvestment(partner,
                profitDistribution.getMonthYear());

        BigDecimal partnerProfit = totalDistributableProfit
                .multiply(partner.getSharePercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        PartnerMonthlyProfit monthlyProfit = new PartnerMonthlyProfit();
        monthlyProfit.setPartner(partner);
        monthlyProfit.setProfitDistribution(profitDistribution);
        monthlyProfit.setInvestmentAmount(effectiveInvestment);
        monthlyProfit.setSharePercentage(partner.getSharePercentage());
        monthlyProfit.setCalculatedProfit(partnerProfit);
        monthlyProfit.setStatus(ProfitStatus.CALCULATED);

        monthlyProfitRepository.save(monthlyProfit);

        log.debug("Calculated profit for partner {}: investment={}, share={}%, profit={}",
                partner.getId(), effectiveInvestment, partner.getSharePercentage(), partnerProfit);
    }

    /**
     * Calculate effective investment considering withdrawals.
     * According to requirement 11: track if withdrawals are from principal or profit.
     */
    private BigDecimal calculateEffectiveInvestment(Partner partner, String distributionMonth) {

        LocalDate monthEnd = LocalDate.parse(distributionMonth + "-01").plusMonths(1).minusDays(1);

        // Get all withdrawals up to this month
        List<PartnerWithdrawal> withdrawals = withdrawalRepository
                .findByPartnerAndRequestedAtBeforeOrderByRequestedAtAsc(partner, monthEnd.atStartOfDay());

        BigDecimal totalInvestment = partner.getTotalInvestment();
        BigDecimal principalWithdrawals = withdrawals.stream()
                .filter(w -> w.getWithdrawalType() == WithdrawalType.FROM_PRINCIPAL)
                .map(PartnerWithdrawal::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal effectiveInvestment = totalInvestment.subtract(principalWithdrawals);

        log.debug("Effective investment for partner {}: total={}, principal withdrawals={}, effective={}",
                partner.getId(), totalInvestment, principalWithdrawals, effectiveInvestment);

        return effectiveInvestment.max(BigDecimal.ZERO);
    }

    /**
     * Get active profit calculation configuration.
     */
    private PartnerProfitCalculationConfig getActiveConfig() {
        return configRepository.findFirstByIsActiveTrueOrderByCreatedAtDesc()
                .orElseThrow(() -> new RuntimeException("No active profit calculation configuration found"));
    }

    /**
     * Manual profit adjustment capability (requirement 9).
     */
    @Transactional
    public void adjustPartnerProfit(Long monthlyProfitId, BigDecimal newAmount, String reason) {
        PartnerMonthlyProfit monthlyProfit = monthlyProfitRepository.findById(monthlyProfitId)
                .orElseThrow(() -> new RuntimeException("Monthly profit record not found"));

        BigDecimal originalAmount = monthlyProfit.getCalculatedProfit();
        monthlyProfit.setCalculatedProfit(newAmount);
        monthlyProfit.setNotes(monthlyProfit.getNotes() +
                String.format(" | Manual adjustment: %s -> %s. Reason: %s",
                        originalAmount, newAmount, reason));

        monthlyProfitRepository.save(monthlyProfit);

        log.info("Manually adjusted profit for partner {}: {} -> {}. Reason: {}",
                monthlyProfit.getPartner().getId(), originalAmount, newAmount, reason);
    }
}
