package com.mahmoud.maalflow.modules.installments.partner.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.modules.installments.capital.entity.CapitalPool;
import com.mahmoud.maalflow.modules.installments.capital.service.CapitalPoolService;
import com.mahmoud.maalflow.modules.installments.partner.entity.Partner;
import com.mahmoud.maalflow.modules.installments.partner.enums.PartnerStatus;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerInvestmentRepository;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerRepository;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerWithdrawalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static com.mahmoud.maalflow.modules.shared.constants.AppConstants.NEW_INVESTMENT_DELAY_MONTHS;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PartnerEffectiveInvestmentService {

    private final PartnerRepository partnerRepository;
    private final PartnerInvestmentRepository investmentRepository;
    private final PartnerWithdrawalRepository withdrawalRepository;
    private final PartnerShareService partnerShareService;
    private final CapitalPoolService capitalPoolService;


    //  Run every month on the 1st at midnight to update effective investments and recalculate shares
    @Scheduled(cron = "0 0 0 1 * ?")
    public void updateAllPartnersEffectiveInvestment() {
        log.info("Starting scheduled task to update effective investment for all partners at {}", LocalDateTime.now());

        LocalDate calculationDate = LocalDate.now();

        List<Partner> activePartners = partnerRepository.findByStatus(PartnerStatus.ACTIVE);
        log.info("Found {} active partners to update", activePartners.size());

        int updatedCount = 0;
        for (Partner partner : activePartners) {
            try {
                BigDecimal newEffectiveInvestment = calculateEffectiveInvestmentForPartner(partner, calculationDate);
                BigDecimal oldEffectiveInvestment = nz(partner.getEffectiveInvestment());
                if (newEffectiveInvestment.compareTo(nz(oldEffectiveInvestment)) != 0) {
                    partner.setEffectiveInvestment(newEffectiveInvestment);
                    partnerRepository.save(partner);
                    updatedCount++;
                    log.debug("Updated effective investment for partner {}: {} -> {}",
                            partner.getId(), partner.getEffectiveInvestment(), newEffectiveInvestment);
                }
            } catch (Exception e) {
                log.error("Failed to update effective investment for partner {}: {}",
                        partner.getId(), e.getMessage(), e);
            }
        }
        // After updating all partners, recalculate share percentages
        if (updatedCount > 0) {
            recalculateAllPartnerShares();
            log.info("Recalculated share percentages after updating {} partners", updatedCount);
        }

        log.info("Completed scheduled task. Updated {} out of {} partners at {}", updatedCount, activePartners.size(), LocalDateTime.now());
    }

    /**
     * Update effective investment for a specific partner (called after investment or withdrawal)
     */
    public void updatePartnerEffectiveInvestment(Long partnerId) {
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new BusinessException("messages.partner.notFound"));

        BigDecimal oldEffectiveInvestment = nz(partner.getEffectiveInvestment());
        BigDecimal newEffectiveInvestment = calculateEffectiveInvestmentForPartner(partner, LocalDate.now());
        if (newEffectiveInvestment.compareTo(oldEffectiveInvestment) != 0) {
            partner.setEffectiveInvestment(newEffectiveInvestment);
            partnerRepository.save(partner);
            log.info("Updated effective investment for partner {}: {} -> {}",
                    partnerId, oldEffectiveInvestment, newEffectiveInvestment);
        }
    }

    /**
     * Calculate effective investment for a partner based on:
     * 1. All confirmed investments that have passed the 2-month delay
     * 2. Minus  withdrawals
     */
    private BigDecimal calculateEffectiveInvestmentForPartner(Partner partner, LocalDate asOfDate) {
        LocalDateTime endOfDay = asOfDate.atTime(LocalTime.MAX); // Use end of day to include all transactions of the day

        // Invested capital becomes eligible after the configured delay.
        LocalDateTime investmentCutoffDateTime = endOfDay.minusMonths(NEW_INVESTMENT_DELAY_MONTHS);

        BigDecimal totalEligibleInvestments = investmentRepository
                .sumConfirmedInvestmentsByPartnerBeforeDate(partner.getId(), investmentCutoffDateTime);

        BigDecimal totalPrincipalWithdrawals = withdrawalRepository
                .sumApprovedOrCompletedWithdrawalsByPartnerUpToDate(partner.getId(), endOfDay);

        // Calculate effective investment
        BigDecimal effectiveInvestment = nz(totalEligibleInvestments)
                .subtract(nz(totalPrincipalWithdrawals))
                .max(BigDecimal.ZERO);

        log.debug("Partner {} effective calculation: eligibleInvestments={}, withdrawals={}, effective={}",
                partner.getId(), totalEligibleInvestments, totalPrincipalWithdrawals, effectiveInvestment);

        return effectiveInvestment;
    }

    /**
     * Recalculate share percentages for all active partners
     */
    private void recalculateAllPartnerShares() {

        CapitalPool capitalPool = capitalPoolService.getPoolOrThrowForUpdate();
        partnerShareService.recalculateSharePercentages(capitalPool.getTotalAmount());
    }


    private BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
