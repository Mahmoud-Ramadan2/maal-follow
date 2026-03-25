package com.mahmoud.maalflow.modules.installments.partner.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerRequest;
import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerResponse;
import com.mahmoud.maalflow.modules.installments.partner.entity.Partner;
import com.mahmoud.maalflow.modules.installments.partner.enums.PartnerStatus;
import com.mahmoud.maalflow.modules.installments.partner.mapper.PartnerMapper;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerRepository;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import com.mahmoud.maalflow.modules.shared.user.repo.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for managing partners.
 * Handles all business logic for partner CRUD operations.
 */
@Service
@AllArgsConstructor
@Slf4j
public class PartnerService {

    private final PartnerRepository partnerRepository;
    private final PartnerMapper partnerMapper;
    private final UserRepository userRepository;

    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    /**
     * Creates a new partner with comprehensive business rule validation.
     *
     * @param request Partner creation request
     * @return Created partner response
     * @throws BusinessException if validation fails
     */
    @Transactional
    public PartnerResponse createPartner(PartnerRequest request) {
        log.info("Creating partner with name: {} and phone: {}", request.getName(), request.getPhone());

        // Business Rule 1: Validate phone number uniqueness
        if (partnerRepository.existsByPhone(request.getPhone())) {
            log.error("Attempt to create partner with duplicate phone number: {}", request.getPhone());
            throw new BusinessException("validation.phone.exists");
        }
       // TODO: SharePercentage should calculated auto based on capital
        // Business Rule 2: Validate share percentage (if provided)
        if (request.getSharePercentage() != null) {
            if (request.getSharePercentage().compareTo(BigDecimal.ZERO) < 0 ||
                request.getSharePercentage().compareTo(BigDecimal.valueOf(100)) > 0) {
                log.error("Invalid share percentage: {}", request.getSharePercentage());
                throw new BusinessException("messages.partner.sharePercentage.invalid");
            }
        }

        // Business Rule 3: Validate investment start date (cannot be in the future)
//        if (request.getInvestmentStartDate() != null &&
//            request.getInvestmentStartDate().isAfter(LocalDate.now())) {
//            log.error("Investment start date cannot be in the future: {}", request.getInvestmentStartDate());
//            throw new BusinessException("messages.partner.investmentStartDate.future");
//        }

        // Business Rule 4: Validate profit calculation start month format and logic
        if (request.getProfitCalculationStartMonth() != null) {
            validateProfitCalculationMonth(request.getProfitCalculationStartMonth(),
                                          request.getInvestmentStartDate());
        }

        // Map request to entity
        Partner partner = partnerMapper.toPartner(request);

        // TODO: Auto set user
        if (request.getCreatedBy() == null){
            partner.setCreatedBy(userRepository.findById(1L).orElse(null)); // Temporary hardcoded user ID
        }
        else {
            boolean exist = userRepository.existsById(request.getCreatedBy());
            if (!exist) {
                log.error("User not found with id: {} so set Defult user during partner creation", request.getCreatedBy());
                partner.setCreatedBy(userRepository.findById(1L).orElse(null)); // Temporary hardcoded user ID
            } else {
                User user = userRepository.findById(request.getCreatedBy()).orElse(null);
                partner.setCreatedBy(user);
            }
        }

        // Business Rule 5: Set default status if not provided
        if (partner.getStatus() == null) {
            partner.setStatus(PartnerStatus.ACTIVE);
        }

        // Business Rule 6: Initialize financial fields with safe defaults
        // Note: totalInvestment, totalWithdrawals, and currentBalance should be auto-calculated
        // based on related transactions, not set directly in the request
        if (partner.getTotalInvestment() == null) {
            partner.setTotalInvestment(BigDecimal.ZERO);
        }
        if (partner.getTotalWithdrawals() == null) {
            partner.setTotalWithdrawals(BigDecimal.ZERO);
        }

        // Business Rule 7: Calculate current balance based on total investment minus total withdrawals
        // In real implementation, these values should be calculated from actual investment and withdrawal records
        partner.setCurrentBalance(
            partner.getTotalInvestment().subtract(partner.getTotalWithdrawals())
        );

        // Business Rule 8: Set profit sharing active by default
        if (partner.getProfitSharingActive() == null) {
            partner.setProfitSharingActive(true);
        }

        // Business Rule 9: Auto-set profit calculation start month if not provided
        // equals to investment start date plus two months
        if (partner.getProfitCalculationStartMonth() == null &&
            partner.getInvestmentStartDate() != null) {
            partner.setProfitCalculationStartMonth(
                partner.getInvestmentStartDate().plusMonths(2).format(MONTH_FORMAT)
            );
            log.debug("Auto-set profit calculation start month to: {}",
                     partner.getProfitCalculationStartMonth());
        }

        // TODO: Set createdBy from security context when authentication is implemented
        // partner.setCreatedBy(getCurrentUser());

        Partner savedPartner = partnerRepository.save(partner);
        log.info("Successfully created partner with ID: {} and name: {}",
                savedPartner.getId(), savedPartner.getName());

        return partnerMapper.toPartnerResponse(savedPartner);
    }

    /**
     * Updates an existing partner with validation.
     *
     * @param id Partner ID
     * @param request Update request
     * @return Updated partner response
     * @throws BusinessException if validation fails or partner not found
     */
    @Transactional
    public PartnerResponse updatePartner(Long id, PartnerRequest request) {
        log.info("Updating partner with ID: {}", id);

        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Partner not found with id: {}", id);
                    return new BusinessException("messages.partner.notFound");
                });

        // Business Rule: Validate phone uniqueness if phone is being changed
        if (request.getPhone() != null && !request.getPhone().equals(partner.getPhone())) {
            if (partnerRepository.existsByPhone(request.getPhone())) {
                log.error("Attempt to update partner with duplicate phone number: {}", request.getPhone());
                throw new BusinessException("messages.partner.phone.duplicate");
            }
            partner.setPhone(request.getPhone());
        }

        // Update fields if provided
        if (request.getName() != null) {
            partner.setName(request.getName());
        }
        if (request.getAddress() != null) {
            partner.setAddress(request.getAddress());
        }
        if (request.getPartnershipType() != null) {
            partner.setPartnershipType(request.getPartnershipType());
        }
        if (request.getSharePercentage() != null) {
            // Validate share percentage
            if (request.getSharePercentage().compareTo(BigDecimal.ZERO) < 0 ||
                    request.getSharePercentage().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new BusinessException("messages.partner.sharePercentage.invalid");
            }
            partner.setSharePercentage(request.getSharePercentage());
        }
        if (request.getStatus() != null) {
            partner.setStatus(request.getStatus());
        }
        if (request.getInvestmentStartDate() != null) {
            partner.setInvestmentStartDate(request.getInvestmentStartDate());
        }
        if (request.getProfitCalculationStartMonth() != null) {
            validateProfitCalculationMonth(request.getProfitCalculationStartMonth(),
                    partner.getInvestmentStartDate());
            partner.setProfitCalculationStartMonth(request.getProfitCalculationStartMonth());
        }
        if (request.getNotes() != null) {
            partner.setNotes(request.getNotes());
        }
        
        // Note: totalInvestment, totalWithdrawals, and currentBalance should be auto-calculated
        // based on actual investment and withdrawal records, not updated directly from the request

        Partner updatedPartner = partnerRepository.save(partner);
        log.info("Successfully updated partner with ID: {}", id);

        return partnerMapper.toPartnerResponse(updatedPartner);
    }


    /**
     * Retrieves a partner by ID.
     *
     * @param id Partner ID
     * @return Partner response
     * @throws BusinessException if partner not found
     */
    @Transactional(readOnly = true)
    public PartnerResponse getPartnerById(Long id) {
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Partner not found with id: {}", id);
                    return new BusinessException("messages.partner.notFound");
                });
        return partnerMapper.toPartnerResponse(partner);
    }

    /**
     * Retrieves all partners.
     *
     * @return List of all partners
     */
    @Transactional(readOnly = true)
    public List<PartnerResponse> getAllPartners() {
        return partnerRepository.findAll()
                .stream()
                .map(partnerMapper::toPartnerResponse)
                .toList();
    }

    /**
     * Retrieves partners filtered by status.
     *
     * @param status Partner status filter
     * @return List of partners with given status
     */
    @Transactional(readOnly = true)
    public List<PartnerResponse> getPartnersByStatus(PartnerStatus status) {
        return partnerRepository.findByStatus(status)
                .stream()
                .map(partnerMapper::toPartnerResponse)
                .toList();
    }


    /**
     * Deletes a partner after validating business rules.
     *
     * @param id Partner ID
     * @throws BusinessException if partner has active balance or not found
     */
    @Transactional
    public void deletePartner(Long id) {
        log.info("Deleting partner with ID: {}", id);

        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Partner not found with id: {}", id);
                    return new BusinessException("messages.partner.notFound");
                });

        // Business Rule: Check if partner has active investments before deletion
        if (partner.getCurrentBalance() != null &&
            partner.getCurrentBalance().compareTo(BigDecimal.ZERO) > 0) {
            log.error("Cannot delete partner with active balance. Partner ID: {}, Balance: {}",
                     id, partner.getCurrentBalance());
            throw new BusinessException("messages.partner.delete.hasBalance");
        }

        partnerRepository.delete(partner);
        log.info("Successfully deleted partner with ID: {}", id);
    }

    /**
     * Validates profit calculation start month format and business logic.
     *
     * @param profitMonth The profit calculation start month (YYYY-MM format)
     * @param investmentStartDate The investment start date
     * @throws BusinessException if validation fails
     */
    private void validateProfitCalculationMonth(String profitMonth, LocalDate investmentStartDate) {
        // Validate format (already validated by @Pattern, but double-check)
        if (!profitMonth.matches("^\\d{4}-\\d{2}$")) {
            throw new BusinessException("validation.profitCalculationStartMonth.pattern");
        }

        // Business Rule: Profit calculation month should not be before investment start month
        if (investmentStartDate != null) {
            String investmentMonth = investmentStartDate.format(MONTH_FORMAT);
            if (profitMonth.compareTo(investmentMonth) < 0) {
                log.error("Profit calculation month {} is before investment start month {}",
                         profitMonth, investmentMonth);
                throw new BusinessException("validation.profitMonth.beforeInvestment");
            }
        }
    }
}

