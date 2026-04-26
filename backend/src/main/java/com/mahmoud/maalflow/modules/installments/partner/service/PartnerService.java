package com.mahmoud.maalflow.modules.installments.partner.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.exception.DuplicateNationalIdException;
import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerInvestmentRequest;
import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerRequest;
import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerResponse;
import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerInvestment;
import com.mahmoud.maalflow.modules.installments.partner.enums.InvestmentStatus;
import com.mahmoud.maalflow.modules.installments.partner.enums.InvestmentType;
import com.mahmoud.maalflow.modules.installments.partner.entity.Partner;
import com.mahmoud.maalflow.modules.installments.partner.enums.PartnerStatus;
import com.mahmoud.maalflow.modules.installments.partner.mapper.PartnerMapper;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerInvestmentRepository;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerRepository;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import com.mahmoud.maalflow.modules.shared.user.repo.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.mahmoud.maalflow.modules.shared.constants.AppConstants.MONTH_FORMAT;

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
    private final PartnerInvestmentService partnerInvestmentService;
    private final PartnerInvestmentRepository partnerInvestmentRepository;

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

        // Business Rule 1: Validate national ID uniqueness
        if (request.getNationalId() != null && !request.getNationalId().isBlank()) {
            if (partnerRepository.existsByNationalId(request.getNationalId())) {
                log.error("Duplicate national ID: {}", request.getNationalId());
                throw new DuplicateNationalIdException("messages.partner.nationalId.exists", request.getNationalId());
            }
        }
        // Business Rule 1: Validate phone number uniqueness
        if (partnerRepository.existsByPhone(request.getPhone())) {
            log.error("Attempt to create partner with duplicate phone number: {}", request.getPhone());
            throw new BusinessException("messages.partner.phone.exists");
        }
        // Business Rule 2: Validate investment start date (cannot be in the future)
        if (request.getInvestmentStartDate() != null &&
                request.getInvestmentStartDate().isAfter(LocalDate.now())) {
            log.error("Investment start date cannot be in the future: {}", request.getInvestmentStartDate());
            throw new BusinessException("messages.partner.investmentStartDate.future");
        }

        // Map request to entity
        Partner partner = partnerMapper.toPartner(request);

        // Business Rule 3: Validate profit calculation start month format and logic
        if (request.getProfitCalculationStartMonth() != null) {
            validateProfitCalculationMonth(request.getProfitCalculationStartMonth(),
                    request.getInvestmentStartDate());
        } else {

            // equals to investment start date plus two months
            partner.setProfitCalculationStartMonth(
                    partner.getInvestmentStartDate().plusMonths(2).format(MONTH_FORMAT)
            );
            log.debug("Auto-set profit calculation start month to: {}",
                    partner.getProfitCalculationStartMonth());
        }
        // Set InvestmentStartDate and ProfitCalculationStartMethod with null  as  they will be set later by admin when confirming the investment.
//        partner.setInvestmentStartDate(null);
//        partner.setProfitCalculationStartMonth(null);


        // TODO: Auto set user
        if (request.getCreatedBy() == null) {
            partner.setCreatedBy(userRepository.findById(1L).orElse(null)); // Temporary hardcoded user ID
        } else {
            boolean exist = userRepository.existsById(request.getCreatedBy());
            if (!exist) {
                log.error("User not found with id: {} so set Defult user during partner creation", request.getCreatedBy());
                partner.setCreatedBy(userRepository.findById(1L).orElse(null)); // Temporary hardcoded user ID
            } else {
                User user = userRepository.findById(request.getCreatedBy()).orElse(null);
                partner.setCreatedBy(user);
            }
        }

        // Business Rule 4: Set default status if not provided
        if (partner.getStatus() == null) {
            partner.setStatus(PartnerStatus.INACTIVE);
        } else {
            partner.setStatus(PartnerStatus.INACTIVE);
        }

        partner.setTotalInvestment(request.getTotalInvestment());
        partner.setTotalWithdrawals(BigDecimal.ZERO);
        partner.setEffectiveInvestment(BigDecimal.ZERO);
        partner.setCurrentBalance(request.getTotalInvestment());

        // Business Rule 6: Set profit sharing active by default
        if (partner.getProfitSharingActive() == null) {
            partner.setProfitSharingActive(true);
        }


        // TODO: Set createdBy from security context when authentication is implemented
        // partner.setCreatedBy(getCurrentUser());

        Partner savedPartner = partnerRepository.save(partner);

        // record investment in partner investment service with pending status
        String notes = "إضافة استثمار أولي للشريك "
                + savedPartner.getName()
                + " (رقم: " + savedPartner.getId() + ")";
        recordPartnerInvestment(nz(request.getTotalInvestment()), partner.getId(), notes);

        // investment will be confirmed later by admin,
        // so shares will be recalculated when confirming the investment.
//        recalculateSharesFromCurrentPool();

        log.info("Successfully created partner with ID: {} and name: {}",
                savedPartner.getId(), savedPartner.getName());

        return partnerMapper.toPartnerResponse(savedPartner);
    }


    /**
     * Updates an existing partner with validation.
     *
     * @param id      Partner ID
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

        if (request.getStatus() != null && request.getStatus().equals(PartnerStatus.INACTIVE)) {
            throw new BusinessException("messages.partner.status.inactiveNotAllowed");
        }
        if (request.getNationalId() != null && !request.getNationalId().equals(partner.getNationalId())) {
            if (partnerRepository.existsByNationalId(request.getNationalId())) {
                throw new DuplicateNationalIdException("messages.partner.nationalId.exists", request.getNationalId());
            }
        }
        // Business Rule: Validate phone uniqueness if phone is being changed
        if (request.getPhone() != null && !request.getPhone().equals(partner.getPhone())) {
            if (partnerRepository.existsByPhone(request.getPhone())) {
                log.error("Attempt to update partner with duplicate phone number: {}", request.getPhone());
                throw new BusinessException("messages.partner.phone.duplicate");
            }
            partner.setPhone(request.getPhone());
        }

        // Update fields if provided
        if (request.getName() != null && !request.getName().equals(partner.getName())) {
            partner.setName(request.getName());
        }
        if (request.getAddress() != null && !request.getAddress().equals(partner.getAddress())) {
            partner.setAddress(request.getAddress());
        }
        if (request.getPartnershipType() != null && !request.getPartnershipType().equals(partner.getPartnershipType())) {
            partner.setPartnershipType(request.getPartnershipType());
        }

        if (request.getInvestmentStartDate() != null && !request.getInvestmentStartDate().equals(partner.getInvestmentStartDate())) {
            partner.setInvestmentStartDate(request.getInvestmentStartDate());
        }
        if (request.getProfitCalculationStartMonth() != null && !request.getProfitCalculationStartMonth().equals(partner.getProfitCalculationStartMonth())) {
            validateProfitCalculationMonth(request.getProfitCalculationStartMonth(),
                    partner.getInvestmentStartDate());
            partner.setProfitCalculationStartMonth(request.getProfitCalculationStartMonth());
        }
        // Financial balances are maintained by investment/withdrawal workflows.
        if (request.getNotes() != null && !request.getNotes().equals(partner.getNotes())) {
            partner.setNotes(request.getNotes());
        }
        // Initial investment amount can be edited only before any confirmed investment exists.
        if (request.getTotalInvestment() != null && request.getTotalInvestment().compareTo(partner.getTotalInvestment()) != 0) {
            handleInitialInvestmentUpdate(partner, request.getTotalInvestment());
        }
        Partner updatedPartner = partnerRepository.save(partner);

        // No update Money so no need to call
//        recalculateSharesFromCurrentPool();
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
     * Soft Delete a partner after validating business rules.
     *
     * @param id Partner ID
     * @throws BusinessException if partner has active balance or not found
     */
    @Transactional
    public void softDeletePartner(Long id) {
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

        partner.setStatus(PartnerStatus.INACTIVE);
        partnerRepository.save(partner);
        log.info("Successfully deleted partner with ID: {}", id);
    }

    /**
     * Validates profit calculation start month format and business logic.
     *
     * @param profitMonth         The profit calculation start month (YYYY-MM format)
     * @param investmentStartDate The investment start date
     * @throws BusinessException if validation fails
     */
    private void validateProfitCalculationMonth(String profitMonth, LocalDate investmentStartDate) {
        // Validate format (already validated by @Pattern, but double-check)
        if (!profitMonth.matches("^\\d{4}-\\d{2}$")) {
            throw new BusinessException("messages.partner.profitCalculationStartMonth.pattern");
        }

        // Business Rule: Profit calculation month should not be before investment start month
        if (investmentStartDate != null) {
            String investmentMonth = investmentStartDate.format(MONTH_FORMAT);
            if (profitMonth.compareTo(investmentMonth) < 0) {
                log.error("Profit calculation month {} is before investment start month {}",
                        profitMonth, investmentMonth);
                throw new BusinessException("messages.partner.profitMonth.beforeInvestment");
            }
        }
    }

//    private void recalculateSharesFromCurrentPool() {
//        CapitalPool pool = capitalPoolRepository.findByIdForUpdate(DEFAULT_POOL_ID).orElse(null);
//        if (pool != null) {
//            partnerShareService.recalculateSharePercentages(nz(pool.getTotalAmount()));
//            return;
//        }
//
//        BigDecimal totalInvestment = nz(partnerRepository.sumTotalInvestment());
//        BigDecimal totalWithdrawals = nz(partnerRepository.sumTotalWithdrawals());
//        partnerShareService.recalculateSharePercentages(totalInvestment.subtract(totalWithdrawals));
//    }

    private void recordPartnerInvestment(BigDecimal totalInvestment, Long id, String notes) {
        PartnerInvestmentRequest investmentRequest = PartnerInvestmentRequest.builder()
                .partnerId(id)
                .amount(totalInvestment)
                .notes(notes)
                .build();
        partnerInvestmentService.createInvestment(investmentRequest);
    }

    private void handleInitialInvestmentUpdate(Partner partner, BigDecimal requestedAmount) {
        BigDecimal newAmount = nz(requestedAmount);
        Long partnerId = partner.getId();

        BigDecimal confirmedTotal = partnerInvestmentRepository
                .sumByPartnerIdAndStatus(partnerId, InvestmentStatus.CONFIRMED);
        BigDecimal safeConfirmedTotal = nz(confirmedTotal);

        if (safeConfirmedTotal.compareTo(BigDecimal.ZERO) > 0 || partner.getStatus() == PartnerStatus.ACTIVE) {
            throw new BusinessException("messages.partner.totalInvestment.update.notAllowed");
        }

        List<PartnerInvestment> pendingInitialInvestments = partnerInvestmentRepository
                .findByPartnerIdAndStatus(partnerId, InvestmentStatus.PENDING)
                .stream()
                .filter(inv -> inv.getInvestmentType() == InvestmentType.INITIAL)
                .toList();

        if (pendingInitialInvestments.isEmpty()) {
            throw new BusinessException("messages.partner.initialInvestment.pending.notFound");
        }

        PartnerInvestment pendingInitial = pendingInitialInvestments.getFirst();
        if (pendingInitial.getAmount().compareTo(newAmount) != 0) {
            log.info("Updating pending initial investment for partner {} from {} to {}",
                    partnerId, pendingInitial.getAmount(), newAmount);
            pendingInitial.setAmount(newAmount);
            partnerInvestmentRepository.save(pendingInitial);
        }

        partner.setTotalInvestment(newAmount);
        partner.setEffectiveInvestment(BigDecimal.ZERO);
        partner.setCurrentBalance(newAmount);
    }

    private BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}


