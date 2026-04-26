package com.mahmoud.maalflow.modules.installments.partner.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.modules.installments.partner.entity.Partner;
import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerProfitCalculationConfig;
import com.mahmoud.maalflow.modules.installments.partner.enums.PartnerStatus;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerProfitCalculationConfigRepository;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerRepository;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import com.mahmoud.maalflow.modules.shared.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.mahmoud.maalflow.modules.shared.constants.AppConstants.MONTH_FORMAT;

/**
 * Service for advanced partner profit sharing control.
 * Implements requirements 9, 10: Dynamic profit sharing control, 2-month delay, manual adjustments.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PartnerProfitControlService {

    private final PartnerRepository partnerRepository;
    private final PartnerProfitCalculationConfigRepository configRepository;
    private final UserRepository userRepository;

    /**
     * Start profit sharing for a partner.
     * Implements requirement: "when do we start adding new subscriber earnings"
     */
    @Transactional
    public void startProfitSharing(Long partnerId, LocalDate startDate) {
        log.info("Starting profit sharing for partner {} from date {}", partnerId, startDate);

        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new BusinessException("messages.partner.notFound"));

        // Calculate profit start month with 2-month delay
        LocalDate profitStartDate = startDate.plusMonths(2);
        String profitStartMonth = profitStartDate.format(MONTH_FORMAT);

        partner.setProfitSharingActive(true);
        partner.setInvestmentStartDate(startDate);
        partner.setProfitCalculationStartMonth(profitStartMonth);

        partnerRepository.save(partner);
        log.info("Profit sharing activated for partner {}. Profits will start from month {}", partnerId, profitStartMonth);
    }


    /**
     * Temporarily pause profit sharing (can be resumed later).
     */
    @Transactional
    public void pauseProfitSharing(Long partnerId, String reason) {
        log.info("Pausing profit sharing for partner {} - Reason: {}", partnerId, reason);

        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new BusinessException("messages.partner.notFound"));

        partner.setProfitSharingActive(false);

        String currentNotes = partner.getNotes() != null ? partner.getNotes() : "";
        partner.setNotes(currentNotes + " | طھظ… ط¥ظٹظ‚ط§ظپ ظ…ط´ط§ط±ظƒط© ط§ظ„ط£ط±ط¨ط§ط­ ظ…ط¤ظ‚طھظ‹ط§: " + reason + " (طھط§ط±ظٹط®: " + LocalDate.now() + ")");

        partnerRepository.save(partner);
        log.info("Profit sharing paused for partner {}", partnerId);
    }

    /**
     * Resume profit sharing for a partner.
     */
    @Transactional
    public void resumeProfitSharing(Long partnerId) {
        log.info("Resuming profit sharing for partner {}", partnerId);

        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new BusinessException("messages.partner.notFound"));

        partner.setProfitSharingActive(true);

        String currentNotes = partner.getNotes() != null ? partner.getNotes() : "";
        partner.setNotes(currentNotes + " | Profit sharing resumed (Date: " + LocalDate.now() + ")");

        partnerRepository.save(partner);
        log.info("Profit sharing resumed for partner {}", partnerId);
    }

    /**
     * Update management fee and zakat percentages.
     * Implements requirement: (manual control of management fee and zakat)
     */
    @Transactional
    public void updateProfitCalculationConfig(BigDecimal managementFeePercentage,
                                             BigDecimal zakatPercentage,
                                             Integer profitPaymentDay) {
        log.info("Updating profit calculation config - Management: {}%, Zakat: {}%, Payment day: {}",
                managementFeePercentage, zakatPercentage, profitPaymentDay);

        // Validate percentages
        if (managementFeePercentage.compareTo(BigDecimal.ZERO) < 0 || managementFeePercentage.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessException("messages.partner.profitCalculation.managementFee.invalid");
        }
        if (zakatPercentage.compareTo(BigDecimal.ZERO) < 0 || zakatPercentage.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessException("messages.partner.profitCalculation.zakat.invalid");
        }
        if (profitPaymentDay < 1 || profitPaymentDay > 28) {
            throw new BusinessException("messages.partner.profitCalculation.paymentDay.invalid");
        }

        User actingUser = userRepository.findById(1L)
                .orElseThrow(() -> new BusinessException("messages.user.notFound"));

        // Deactivate current config
        configRepository.findFirstByIsActiveTrueOrderByCreatedAtDesc()
                .ifPresent(config -> {
                    config.setIsActive(false);
                    config.setUpdatedBy(actingUser);
                    configRepository.save(config);
                });

        // Create new config
        PartnerProfitCalculationConfig newConfig = new PartnerProfitCalculationConfig();
        newConfig.setManagementFeePercentage(managementFeePercentage);
        newConfig.setZakatPercentage(zakatPercentage);
        newConfig.setProfitPaymentDay(profitPaymentDay);
        newConfig.setIsActive(true);
        newConfig.setNotes("Manual update - Management: " + managementFeePercentage + "%, Zakat: " + zakatPercentage + "%");
        newConfig.setCreatedBy(actingUser);
        newConfig.setUpdatedBy(actingUser);

        configRepository.save(newConfig);
        log.info("Created new profit calculation configuration");
    }

    /**
     * Check if partner is eligible for profit sharing.
     */
    @Transactional(readOnly = true)
    public boolean isPartnerEligibleForProfit(Long partnerId, String distributionMonth) {
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new BusinessException("messages.partner.notFound"));

        if (!partner.getProfitSharingActive()) {
            return false;
        }

        if (partner.getProfitCalculationStartMonth() == null) {
            return false;
        }

        return partner.getProfitCalculationStartMonth().compareTo(distributionMonth) <= 0;
    }

    /**
     * Get current profit calculation configuration.
     */
    @Transactional(readOnly = true)
    public PartnerProfitCalculationConfig getCurrentConfig() {
        return configRepository.findFirstByIsActiveTrueOrderByCreatedAtDesc()
                .orElseThrow(() -> new BusinessException("messages.partner.profitCalculation.config.notFound"));
    }
}

