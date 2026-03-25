package com.mahmoud.maalflow.modules.installments.partner.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.modules.installments.customer.entity.Customer;
import com.mahmoud.maalflow.modules.installments.customer.repo.CustomerRepository;
import com.mahmoud.maalflow.modules.installments.partner.entity.Partner;
import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerCustomerAcquisition;
import com.mahmoud.maalflow.modules.installments.partner.enums.CustomerAcquisitionStatus;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerCustomerAcquisitionRepository;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing partner customer acquisition and commissions.
 * Implements requirement 8: "إضافة عملاء يعملون بالأقساط لحسابي ولهم نسبة"
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PartnerCustomerAcquisitionService {

    private final PartnerCustomerAcquisitionRepository acquisitionRepository;
    private final PartnerRepository partnerRepository;
    private final CustomerRepository customerRepository;

    /**
     * Assign a customer to a partner with commission tracking.
     * Implements requirement: "كيفية إضافة عملاء يعملون بالأقساط لحسابي ولهم نسبة"
     */
    @Transactional
    public PartnerCustomerAcquisition assignCustomerToPartner(Long partnerId, Long customerId,
                                                             BigDecimal commissionPercentage,
                                                             String notes) {
        log.info("Assigning customer {} to partner {} with commission {}%", customerId, partnerId, commissionPercentage);

        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new BusinessException("validation.partner.notFound"));

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException("validation.customer.notFound"));

        // Check if customer is already assigned to another partner
        Optional<PartnerCustomerAcquisition> existingAcquisition = acquisitionRepository
                .findByCustomerIdAndStatus(customerId, CustomerAcquisitionStatus.ACTIVE);

        if (existingAcquisition.isPresent()) {
            throw new BusinessException("validation.customer.alreadyAssigned");
        }

        // Validate commission percentage
        if (commissionPercentage.compareTo(BigDecimal.ZERO) < 0 ||
            commissionPercentage.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessException("validation.commission.percentage.invalid");
        }

        PartnerCustomerAcquisition acquisition = new PartnerCustomerAcquisition();
        acquisition.setPartner(partner);
        acquisition.setCustomer(customer);
        acquisition.setCommissionPercentage(commissionPercentage);
        acquisition.setStatus(CustomerAcquisitionStatus.ACTIVE);
        acquisition.setAcquisitionNotes(notes);

        PartnerCustomerAcquisition saved = acquisitionRepository.save(acquisition);
        log.info("Successfully assigned customer {} to partner {}", customerId, partnerId);

        return saved;
    }

    /**
     * Transfer customer from one partner to another.
     */
    @Transactional
    public void transferCustomer(Long customerId, Long fromPartnerId, Long toPartnerId, String reason) {
        log.info("Transferring customer {} from partner {} to partner {}", customerId, fromPartnerId, toPartnerId);

        // Deactivate current assignment
        PartnerCustomerAcquisition currentAcquisition = acquisitionRepository
                .findByPartnerIdAndCustomerIdAndStatus(fromPartnerId, customerId, CustomerAcquisitionStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException("validation.acquisition.notFound"));

        currentAcquisition.setStatus(CustomerAcquisitionStatus.TRANSFERRED);
        currentAcquisition.setDeactivatedAt(LocalDateTime.now());
        acquisitionRepository.save(currentAcquisition);

        // Create new assignment
        Partner newPartner = partnerRepository.findById(toPartnerId)
                .orElseThrow(() -> new BusinessException("validation.partner.notFound"));

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException("validation.customer.notFound"));

        PartnerCustomerAcquisition newAcquisition = new PartnerCustomerAcquisition();
        newAcquisition.setPartner(newPartner);
        newAcquisition.setCustomer(customer);
        newAcquisition.setCommissionPercentage(currentAcquisition.getCommissionPercentage());
        newAcquisition.setStatus(CustomerAcquisitionStatus.ACTIVE);
        newAcquisition.setAcquisitionNotes("Transferred from partner " + fromPartnerId + ". Reason: " + reason);

        acquisitionRepository.save(newAcquisition);
        log.info("Successfully transferred customer {} from partner {} to partner {}", customerId, fromPartnerId, toPartnerId);
    }

    /**
     * Get all customers assigned to a partner.
     */
    @Transactional(readOnly = true)
    public List<PartnerCustomerAcquisition> getPartnerCustomers(Long partnerId) {
        return acquisitionRepository.findByPartnerIdAndStatus(partnerId, CustomerAcquisitionStatus.ACTIVE);
    }

    /**
     * Get partner performance metrics.
     * Implements requirement: tracking customer acquisition and commission earnings.
     */
    @Transactional(readOnly = true)
    public PartnerPerformanceMetrics getPartnerPerformance(Long partnerId) {
        List<PartnerCustomerAcquisition> activeCustomers = getPartnerCustomers(partnerId);

        BigDecimal totalCommissionEarned = activeCustomers.stream()
                .map(PartnerCustomerAcquisition::getTotalCommissionEarned)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return PartnerPerformanceMetrics.builder()
                .partnerId(partnerId)
                .totalActiveCustomers(activeCustomers.size())
                .totalCommissionEarned(totalCommissionEarned)
                .averageCommissionPercentage(calculateAverageCommission(activeCustomers))
                .build();
    }

    /**
     * Update commission earned for a partner-customer relationship.
     */
    @Transactional
    public void updateCommissionEarned(Long partnerId, Long customerId, BigDecimal commissionAmount) {
        PartnerCustomerAcquisition acquisition = acquisitionRepository
                .findByPartnerIdAndCustomerIdAndStatus(partnerId, customerId, CustomerAcquisitionStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException("validation.acquisition.notFound"));

        BigDecimal currentTotal = acquisition.getTotalCommissionEarned();
        acquisition.setTotalCommissionEarned(currentTotal.add(commissionAmount));

        acquisitionRepository.save(acquisition);
        log.info("Updated commission for partner {} customer {}: added {}", partnerId, customerId, commissionAmount);
    }

    private BigDecimal calculateAverageCommission(List<PartnerCustomerAcquisition> acquisitions) {
        if (acquisitions.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = acquisitions.stream()
                .map(PartnerCustomerAcquisition::getCommissionPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(BigDecimal.valueOf(acquisitions.size()), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Performance metrics DTO for partners.
     */
    @lombok.Builder
    @lombok.Data
    public static class PartnerPerformanceMetrics {
        private Long partnerId;
        private int totalActiveCustomers;
        private BigDecimal totalCommissionEarned;
        private BigDecimal averageCommissionPercentage;
    }
}
