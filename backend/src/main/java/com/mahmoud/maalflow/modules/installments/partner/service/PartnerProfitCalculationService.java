package com.mahmoud.maalflow.modules.installments.partner.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.modules.installments.partner.entity.Partner;
import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerMonthlyProfit;
import com.mahmoud.maalflow.modules.installments.partner.enums.PartnerStatus;
import com.mahmoud.maalflow.modules.installments.partner.enums.ProfitStatus;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerInvestmentRepository;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerMonthlyProfitRepository;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerRepository;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerWithdrawalRepository;
import com.mahmoud.maalflow.modules.installments.profit.entity.MonthlyProfitDistribution;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.mahmoud.maalflow.modules.shared.constants.AppConstants.NEW_INVESTMENT_DELAY_MONTHS;

/**
 *
 */
@Service
@AllArgsConstructor
@Slf4j
public class PartnerProfitCalculationService {

    private final PartnerRepository partnerRepository;
    private final PartnerMonthlyProfitRepository partnerMonthlyProfitRepository;
    private final PartnerInvestmentRepository partnerInvestmentRepository;
    private final PartnerWithdrawalRepository partnerWithdrawalRepository;

    /**
     * Calculate and distribute monthly profits for all active partners according to business rules:
     * Called by MonthlyProfitDistributionService.distributeProfit
     */
    @Transactional
    public void calculateAndDistributeMonthlyProfits(MonthlyProfitDistribution profitDistribution) {
        log.info("Starting monthly profit calculation for partners for period: {}",
                profitDistribution.getMonthYear());
        BigDecimal totalDistributableProfit = profitDistribution.getDistributableProfit();

        // TODO change to lower value may be 100 or 1000
        if(totalDistributableProfit.compareTo(BigDecimal.ZERO) < 0) {
            log.info("Calculated distributable profit is negative: {}. Setting to zero.", totalDistributableProfit);
            throw new BusinessException("messages.partner.monthlyProfit.distributable.invalid");
        }

        // Enforce single period cutoff policy at month end for all snapshot queries.
        YearMonth distributionYearMonth = YearMonth.parse(profitDistribution.getMonthYear());
        LocalDate distributionMonthStart = distributionYearMonth.atDay(1);
        LocalDateTime distributionMonthEnd = distributionYearMonth.atEndOfMonth().atTime(LocalTime.MAX);

        List<Partner> candidatePartners = partnerRepository.findByProfitSharingActive(true);

        Map<Long, BigDecimal> effectiveSnapshotByPartnerId = new LinkedHashMap<>();
        List<Partner> eligiblePartners = new ArrayList<>();
        List<String> exclusionNotes = new ArrayList<>();

        for (Partner partner : candidatePartners.stream().sorted(Comparator.comparing(Partner::getId)).toList()) {

            if (partner.getStatus() == PartnerStatus.INACTIVE) {
                exclusionNotes.add(String.format("تم استبعاد الشريك رقم %d من احتساب أرباح شهر %s بسبب توقف نشاطه.",
                        partner.getId(), profitDistribution.getMonthYear()));
                continue;
            }

            BigDecimal effectiveInvestmentSnapshot = calculateEffectiveInvestmentAsOf(partner.getId(), distributionMonthEnd);
            if (effectiveInvestmentSnapshot.compareTo(BigDecimal.ZERO) <= 0) {
                exclusionNotes.add(String.format("تم استبعاد الشريك رقم %d من احتساب أرباح شهر %s لعدم وجود استثمار فعال (صافي الاستثمار = %s).",
                        partner.getId(), profitDistribution.getMonthYear(), effectiveInvestmentSnapshot));
                continue;
            }

            eligiblePartners.add(partner);
            effectiveSnapshotByPartnerId.put(partner.getId(), effectiveInvestmentSnapshot);
        }

        appendCalculationNotes(profitDistribution, exclusionNotes);

        if (eligiblePartners.isEmpty()) {
            log.info("No eligible partners for distribution month {}", profitDistribution.getMonthYear());
            throw new BusinessException("messages.partner.monthlyProfit.eligiblePartner.empty");
        }

        BigDecimal totalEffectiveInvestment = effectiveSnapshotByPartnerId.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalEffectiveInvestment.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("Total effective investment is zero or negative for distribution month {}. Cannot distribute profit.",
                    profitDistribution.getMonthYear());
            throw new BusinessException("messages.partner.monthlyProfit.share.invalid");
        }

        BigDecimal assignedProfit = BigDecimal.ZERO;
        for (int i = 0; i < eligiblePartners.size(); i++) {
            Partner partner = eligiblePartners.get(i);
            BigDecimal partnerEffectiveInvestment = effectiveSnapshotByPartnerId.get(partner.getId());
            BigDecimal frozenSharePercentage = partnerEffectiveInvestment
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalEffectiveInvestment, 2, RoundingMode.HALF_UP);

            BigDecimal partnerProfit;
            if (i == eligiblePartners.size() - 1) {
                // Assign the final remainder to ensure exact total reconciliation.
                partnerProfit = totalDistributableProfit.subtract(assignedProfit).setScale(2, RoundingMode.HALF_UP);
            } else {
                partnerProfit = totalDistributableProfit
                        .multiply(partnerEffectiveInvestment)
                        .divide(totalEffectiveInvestment, 10, RoundingMode.HALF_UP)
                        .setScale(2, RoundingMode.DOWN);
                assignedProfit = assignedProfit.add(partnerProfit);
                log.info("Calculated profit for partner {}: effectiveInvestment={}, frozenShare={}%, profit={}, assignedProfit={}",
                        partner.getId(), partnerEffectiveInvestment, frozenSharePercentage, partnerProfit, assignedProfit);
            }

            calculatePartnerMonthlyProfit(partner, profitDistribution, partnerProfit,
                    partnerEffectiveInvestment, frozenSharePercentage);
        }

        log.info("Completed monthly profit calculation for {} eligible partners", eligiblePartners.size());
    }
    /**
     * Calculate individual partner's monthly profit.
     */
    private void calculatePartnerMonthlyProfit(Partner partner,
                                             MonthlyProfitDistribution profitDistribution,
                                             BigDecimal partnerProfit,
                                             BigDecimal frozenEffectiveInvestment,
                                             BigDecimal frozenSharePercentage) {



        PartnerMonthlyProfit monthlyProfit = new PartnerMonthlyProfit();
        monthlyProfit.setPartner(partner);
        monthlyProfit.setProfitDistribution(profitDistribution);
        monthlyProfit.setInvestmentAmount(nz(frozenEffectiveInvestment));
        monthlyProfit.setSharePercentage(nz(frozenSharePercentage));
        monthlyProfit.setCalculatedProfit(partnerProfit);
        monthlyProfit.setStatus(ProfitStatus.CALCULATED);

        partnerMonthlyProfitRepository.save(monthlyProfit);

        log.debug("Calculated profit for partner {}: investment={}, share={}%, profit={}",
                partner.getId(), frozenEffectiveInvestment, frozenSharePercentage, partnerProfit);
    }


    /**
     * Manual profit adjustment capability (requirement 9).
     */
    @Transactional
    public void adjustPartnerProfit(Long monthlyProfitId, BigDecimal newAmount, String reason) {
        PartnerMonthlyProfit monthlyProfit = partnerMonthlyProfitRepository.findById(monthlyProfitId)
                .orElseThrow(() -> new BusinessException("messages.partner.monthlyProfit.notFound"));

        BigDecimal originalAmount = monthlyProfit.getCalculatedProfit();

        if (originalAmount.compareTo(newAmount) == 0) {
            throw new BusinessException("messages.partner.monthlyProfit.adjustment.noChange");
        }
        else if (newAmount.compareTo(BigDecimal.ZERO) < 0) {
            log.info("Adjusting profit for partner {}: original amount {} -> new amount {}. Reason: {}",
                    monthlyProfit.getPartner().getId(), originalAmount, newAmount, reason);
        }
        else {
            // new amount > old
            log.info("Adjusting profit for partner {}: original amount {} -> new amount {}. Reason: {}",
                    monthlyProfit.getPartner().getId(), originalAmount, newAmount, reason);

        }

        monthlyProfit.setCalculatedProfit(newAmount);

        String currentNotes = monthlyProfit.getNotes() == null ? "" : monthlyProfit.getNotes();
        String newNote = String.format(" | تعديل الربح يدويا من %s -> %s. السبب: %s",
                originalAmount, newAmount, reason);

        monthlyProfit.setNotes(currentNotes + newNote);

        partnerMonthlyProfitRepository.save(monthlyProfit);

        // TODO wht other entity should be updated i

        log.info("Manually adjusted profit for partner {}: {} -> {}. Reason: {}",
                monthlyProfit.getPartner().getId(), originalAmount, newAmount, reason);
    }

    private BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal calculateEffectiveInvestmentAsOf(Long partnerId, LocalDateTime distributionMonthEnd) {
        LocalDateTime investmentCutoff = distributionMonthEnd.minusMonths(NEW_INVESTMENT_DELAY_MONTHS);

        BigDecimal eligibleInvestments = partnerInvestmentRepository
                .sumConfirmedInvestmentsByPartnerBeforeDate(partnerId, investmentCutoff);

        BigDecimal approvedPrincipalWithdrawals = partnerWithdrawalRepository
                .sumApprovedOrCompletedWithdrawalsByPartnerUpToDate(partnerId, distributionMonthEnd);

        return nz(eligibleInvestments).subtract(nz(approvedPrincipalWithdrawals)).max(BigDecimal.ZERO);
    }

//    private boolean isInactivatedWithinDistributionMonth(Partner partner,
//                                                         LocalDate distributionMonthStart,
//                                                         LocalDateTime distributionMonthEnd) {
//        if (partner.getStatus() == PartnerStatus.ACTIVE || partner.getUpdatedAt() == null) {
//            return false;
//        }
//
//        LocalDateTime startOfMonth = distributionMonthStart.atStartOfDay();
//        LocalDateTime updatedAt = partner.getUpdatedAt();
//        return !updatedAt.isBefore(startOfMonth) && !updatedAt.isAfter(distributionMonthEnd);
//    }

    private void appendCalculationNotes(MonthlyProfitDistribution distribution, List<String> notes) {
        if (notes.isEmpty()) {
            return;
        }

        String existing = distribution.getCalculationNotes();
        String joined = String.join(" | ", notes);
        distribution.setCalculationNotes((existing == null || existing.isBlank()) ? joined : existing + " | " + joined);
    }
}
