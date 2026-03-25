package com.mahmoud.maalflow.modules.installments.payment.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.modules.installments.payment.dto.PaymentDiscountConfigRequest;
import com.mahmoud.maalflow.modules.installments.payment.dto.PaymentDiscountConfigResponse;
import com.mahmoud.maalflow.modules.installments.payment.entity.PaymentDiscountConfig;
import com.mahmoud.maalflow.modules.installments.payment.enums.DiscountType;
import com.mahmoud.maalflow.modules.installments.payment.mapper.PaymentDiscountConfigMapper;
import com.mahmoud.maalflow.modules.installments.payment.repo.PaymentDiscountConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for calculating payment discounts.
* Implements requirement 7: “A mechanism for making a discount when paying early or in the last installment.” */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentDiscountService {

    private final PaymentDiscountConfigRepository discountConfigRepository;
    private final PaymentDiscountConfigMapper discountConfigMapper;

    /**
     * Calculate discount for early payment.
* Implements: "A mechanism for making a discount when paying early"
     * */
    @Transactional(readOnly = true)
    public BigDecimal calculateEarlyPaymentDiscount(BigDecimal amount, LocalDate paymentDate, LocalDate dueDate) {
        log.info("Calculating early payment discount for amount: {}, payment date: {}, due date: {}",
                amount, paymentDate, dueDate);

        // only early payments if before 10 days of due date

        if (!isEarlyPayment(paymentDate, dueDate)) {
            log.debug("Payment is not early, no discount applicable");
            return BigDecimal.ZERO;
        }

        PaymentDiscountConfig config = getActiveDiscountConfig(DiscountType.EARLY_PAYMENT);
        if (config == null) {
            log.debug("No active early payment discount configuration found");
            return BigDecimal.ZERO;
        }

        long daysBefore = ChronoUnit.DAYS.between(paymentDate, dueDate);
        if (daysBefore < config.getEarlyPaymentDaysThreshold()) {
            log.debug("Payment is only {} days early, threshold is {}", daysBefore, config.getEarlyPaymentDaysThreshold());
            return BigDecimal.ZERO;
        }

        BigDecimal discount = amount
                .multiply(config.getEarlyPaymentDiscountPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Apply minimum and maximum limits
        discount = applyDiscountLimits(discount, config);

        log.info("Calculated early payment discount: {} for {} days early payment", discount, daysBefore);
        return discount;
    }

    /**
     * Check if payment is early.
     * Early payment is defined as payment made before the due date with 10 days.
     */
    public boolean isEarlyPayment(LocalDate actualPaymentDate, LocalDate dueDate) {

        // Early payment is defined as payment made before the due date with 10 days
        LocalDate earlyPaymentThreshold = dueDate.minusDays(10);
        return earlyPaymentThreshold.isBefore(actualPaymentDate);
    }
    /**
     * Calculate discount for final installment payment.
* Implements: "A mechanism for making a discount in the last installment"     */
    @Transactional(readOnly = true)
    public BigDecimal calculateFinalInstallmentDiscount(BigDecimal amount, boolean isFinalInstallment) {
        log.info("Calculating final installment discount for amount: {}, is final: {}", amount, isFinalInstallment);

        if (!isFinalInstallment) {
            log.debug("Not a final installment, no discount applicable");
            return BigDecimal.ZERO;
        }

        PaymentDiscountConfig config = getActiveDiscountConfig(DiscountType.FINAL_INSTALLMENT);
        if (config == null) {
            log.debug("No active final installment discount configuration found");
            return BigDecimal.ZERO;
        }

        BigDecimal discount = amount
                .multiply(config.getFinalInstallmentDiscountPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Apply minimum and maximum limits
        discount = applyDiscountLimits(discount, config);

        log.info("Calculated final installment discount: {}", discount);
        return discount;
    }

    /**
     * Calculate total applicable discount combining early payment and final installment.
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateHigherDiscount(BigDecimal amount, LocalDate paymentDate,
                                           LocalDate dueDate, boolean isFinalInstallment) {
        log.info("Calculating total discount for amount: {}", amount);

        BigDecimal earlyDiscount = calculateEarlyPaymentDiscount(amount, paymentDate, dueDate);
        BigDecimal finalDiscount = calculateFinalInstallmentDiscount(amount, isFinalInstallment);

        // Apply the higher discount (not both)
        BigDecimal totalDiscount = earlyDiscount.max(finalDiscount);

        log.info("Total discount calculated: {} (early: {}, final: {})", totalDiscount, earlyDiscount, finalDiscount);
        return totalDiscount;
    }

    /**
     * Create or update discount configuration from DTO.
     * This is the main endpoint method that returns a response DTO.
     */
    @Transactional
    public PaymentDiscountConfigResponse saveDiscountConfig(PaymentDiscountConfigRequest request) {
        log.info("Saving discount configuration from request: {}", request.getDiscountType());

        // Convert request DTO to entity
        PaymentDiscountConfig config = discountConfigMapper.toEntity(request);
        config.setIsActive(true);

        // Deactivate existing config of same type
        discountConfigRepository.findByDiscountTypeAndIsActiveTrue(config.getDiscountType())
                .ifPresent(existingConfig -> {
                    existingConfig.setIsActive(false);
                    discountConfigRepository.save(existingConfig);
                    log.info("Deactivated existing config for type: {}", config.getDiscountType());
                });

        PaymentDiscountConfig saved = discountConfigRepository.save(config);
        log.info("Saved new discount configuration with ID: {}", saved.getId());

        return discountConfigMapper.toResponse(saved);
    }

    /**
     * Get active discount configuration by type.
     */
    private PaymentDiscountConfig getActiveDiscountConfig(DiscountType discountType) {
        return discountConfigRepository.findByDiscountTypeAndIsActiveTrue(discountType)
                .orElse(null);
    }

    /**
     * Get discount configuration by type (for endpoint).
     */
    @Transactional(readOnly = true)
    public PaymentDiscountConfigResponse getDiscountConfig(DiscountType discountType) {
        log.info("Fetching discount configuration for type: {}", discountType);

        PaymentDiscountConfig config = discountConfigRepository.findByDiscountTypeAndIsActiveTrue(discountType)
                .orElseThrow(() -> new BusinessException("No active discount configuration found for type: " + discountType));

        return discountConfigMapper.toResponse(config);
    }

    /**
     * Deactivate discount configuration by type (for endpoint).
     */
    @Transactional
    public void deactivateDiscountConfig(DiscountType discountType) {
        log.info("Deactivating discount configuration for type: {}", discountType);

        PaymentDiscountConfig config = discountConfigRepository.findByDiscountTypeAndIsActiveTrue(discountType)
                .orElseThrow(() -> new BusinessException("No active discount configuration found for type: " + discountType));

        config.setIsActive(false);
        discountConfigRepository.save(config);

        log.info("Deactivated discount configuration for type: {}", discountType);
    }

    /**
     * Apply minimum and maximum discount limits.
     */
    private BigDecimal applyDiscountLimits(BigDecimal discount, PaymentDiscountConfig config) {
        if (config.getMinimumDiscountAmount() != null &&
            discount.compareTo(config.getMinimumDiscountAmount()) < 0) {
            discount = config.getMinimumDiscountAmount();
        }

        if (config.getMaximumDiscountAmount() != null &&
            discount.compareTo(config.getMaximumDiscountAmount()) > 0) {
            discount = config.getMaximumDiscountAmount();
        }

        return discount;
    }

    /**
     * Get all active discount configurations.
     */
    @Transactional(readOnly = true)
    public List<PaymentDiscountConfigResponse> getAllActiveDiscountConfigs() {
        log.info("Fetching all active discount configurations");

        List<PaymentDiscountConfig> activeConfigs = discountConfigRepository.findAllByIsActiveTrue();
        return activeConfigs.stream()
                .map(discountConfigMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Activate a discount configuration by ID.
     */
    @Transactional
    public PaymentDiscountConfigResponse activateDiscountConfig(Long id) {
        log.info("Activating discount configuration with ID: {}", id);

        PaymentDiscountConfig config = discountConfigRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Discount configuration not found"));

        config.setIsActive(true);
        PaymentDiscountConfig savedConfig = discountConfigRepository.save(config);

        log.info("Activated discount configuration with ID: {}", savedConfig.getId());
        return discountConfigMapper.toResponse(savedConfig);
    }

}
