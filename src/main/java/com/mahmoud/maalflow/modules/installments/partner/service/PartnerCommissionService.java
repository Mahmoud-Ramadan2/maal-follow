package com.mahmoud.maalflow.modules.installments.partner.service;

import com.mahmoud.maalflow.modules.installments.contract.entity.Contract;
import com.mahmoud.maalflow.modules.installments.contract.repo.ContractRepository;
import com.mahmoud.maalflow.modules.installments.customer.entity.Customer;
import com.mahmoud.maalflow.modules.installments.customer.repo.CustomerRepository;
import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerCommissionRequest;
import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerCommissionResponse;
import com.mahmoud.maalflow.modules.installments.partner.dto.PartnerCommissionSummary;
import com.mahmoud.maalflow.modules.installments.partner.entity.Partner;
import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerCommission;
import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerCustomerAcquisition;
import com.mahmoud.maalflow.modules.installments.partner.entity.PartnerProfitCalculationConfig;
import com.mahmoud.maalflow.modules.installments.partner.enums.CommissionStatus;
import com.mahmoud.maalflow.modules.installments.partner.enums.CommissionType;
import com.mahmoud.maalflow.modules.installments.partner.mapper.PartnerCommissionMapper;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerCommissionRepository;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerCustomerAcquisitionRepository;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerProfitCalculationConfigRepository;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerRepository;
import com.mahmoud.maalflow.modules.installments.purchase.entity.Purchase;
import com.mahmoud.maalflow.modules.installments.purchase.repo.PurchaseRepository;
import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import com.mahmoud.maalflow.modules.shared.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing partner commissions including calculation, approval, and payment.
 * Handles various types of commissions such as customer acquisition, sales, and referral bonuses.
 *
 * @author Mahmoud
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PartnerCommissionService {

    private final PartnerCommissionRepository partnerCommissionRepository;
    private final PartnerRepository partnerRepository;
    private final ContractRepository contractRepository;
    private final PurchaseRepository purchaseRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final PartnerCustomerAcquisitionRepository partnerCustomerAcquisitionRepository;
    private final PartnerProfitCalculationConfigRepository configRepository;
    private final PartnerCommissionMapper partnerCommissionMapper;

    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    /**
     * Creates a new partner commission based on the request.
     */
    public PartnerCommissionResponse createCommission(PartnerCommissionRequest request) {
        log.info("Creating commission for partner {} with type {}", request.getPartnerId(), request.getCommissionType());

        validateCommissionRequest(request);

        Partner partner = getPartnerById(request.getPartnerId());

        PartnerCommission commission = buildCommissionFromRequest(request, partner);
        calculateCommissionAmount(commission, request);

        PartnerCommission savedCommission = partnerCommissionRepository.save(commission);
        log.info("Commission created with ID {}", savedCommission.getId());

        return partnerCommissionMapper.toPartnerCommissionResponse(savedCommission);
    }

    /**
     * Calculates and creates a customer acquisition commission for a partner.
     */
    public PartnerCommissionResponse createCustomerAcquisitionCommission(Long partnerId, Long customerId, BigDecimal contractValue) {
        log.info("Creating customer acquisition commission for partner {} and customer {}", partnerId, customerId);

        Partner partner = getPartnerById(partnerId);
        Customer customer = getCustomerById(customerId);

        // Check if commission already exists for this customer
        if (commissionExistsForCustomer(partnerId, customerId)) {
            throw new BusinessException("messages.commission.alreadyExists");
        }

        // Get acquisition configuration
        PartnerCustomerAcquisition acquisition = getPartnerCustomerAcquisition(partnerId, customerId);
        if (acquisition == null) {
            throw new BusinessException("messages.commission.noAcquisitionRecord");
        }

        PartnerCommission commission = PartnerCommission.builder()
                .partner(partner)
                .customer(customer)
                .commissionType(CommissionType.CUSTOMER_ACQUISITION)
                .baseAmount(contractValue)
                .commissionPercentage(acquisition.getCommissionPercentage())
                .status(CommissionStatus.PENDING)
                .calculatedAt(LocalDateTime.now())
                .notes("Customer acquisition commission for customer: " + customer.getName())
                .build();

        calculateCommissionAmount(commission);

        PartnerCommission savedCommission = partnerCommissionRepository.save(commission);
        log.info("Customer acquisition commission created with ID {}", savedCommission.getId());

        return partnerCommissionMapper.toPartnerCommissionResponse(savedCommission);
    }

    /**
     * Calculates and creates a sales commission for a contract.
     */
    public PartnerCommissionResponse createSalesCommission(Long partnerId, Long contractId) {
        log.info("Creating sales commission for partner {} and contract {}", partnerId, contractId);

        Partner partner = getPartnerById(partnerId);
        Contract contract = getContractById(contractId);

        // Check if commission already exists for this contract
        if (commissionExistsForContract(partnerId, contractId)) {
            throw new BusinessException("messages.commission.alreadyExists");
        }

        // Get default sales commission percentage from config
        BigDecimal salesCommissionPercentage = getDefaultSalesCommissionPercentage();

        PartnerCommission commission = PartnerCommission.builder()
                .partner(partner)
                .contract(contract)
                .customer(contract.getCustomer())
                .commissionType(CommissionType.SALES_COMMISSION)
                .baseAmount(contract.getFinalPrice())
                .commissionPercentage(salesCommissionPercentage)
                .status(CommissionStatus.PENDING)
                .calculatedAt(LocalDateTime.now())
                .notes("Sales commission for contract: " + contract.getContractNumber())
                .build();

        calculateCommissionAmount(commission);

        PartnerCommission savedCommission = partnerCommissionRepository.save(commission);
        log.info("Sales commission created with ID {}", savedCommission.getId());

        return partnerCommissionMapper.toPartnerCommissionResponse(savedCommission);
    }

    /**
     * Calculates and creates a referral bonus commission.
     */
    public PartnerCommissionResponse createReferralBonus(Long partnerId, BigDecimal bonusAmount, String notes) {
        log.info("Creating referral bonus for partner {} with amount {}", partnerId, bonusAmount);

        Partner partner = getPartnerById(partnerId);

        PartnerCommission commission = PartnerCommission.builder()
                .partner(partner)
                .commissionType(CommissionType.REFERRAL_BONUS)
                .commissionAmount(bonusAmount)
                .baseAmount(bonusAmount)
                .commissionPercentage(BigDecimal.valueOf(100)) // 100% since it's a fixed bonus
                .status(CommissionStatus.PENDING)
                .calculatedAt(LocalDateTime.now())
                .notes(notes != null ? notes : "Referral bonus")
                .build();

        PartnerCommission savedCommission = partnerCommissionRepository.save(commission);
        log.info("Referral bonus created with ID {}", savedCommission.getId());

        return partnerCommissionMapper.toPartnerCommissionResponse(savedCommission);
    }

    /**
     * Approves a commission and marks it for payment.
     */
    public PartnerCommissionResponse approveCommission(Long commissionId, Long approvedByUserId) {
        log.info("Approving commission {} by user {}", commissionId, approvedByUserId);

        PartnerCommission commission = getCommissionById(commissionId);
        User approvedBy = getUserById(approvedByUserId);

        if (commission.getStatus() != CommissionStatus.PENDING) {
            throw new BusinessException("messages.commission.notPending");
        }

        commission.setStatus(CommissionStatus.PAID);
        commission.setApprovedBy(approvedBy);
        commission.setPaidAt(LocalDateTime.now());

        PartnerCommission savedCommission = partnerCommissionRepository.save(commission);
        log.info("Commission {} approved successfully", commissionId);

        return partnerCommissionMapper.toPartnerCommissionResponse(savedCommission);
    }

    /**
     * Cancels a commission.
     */
    public void cancelCommission(Long commissionId, String reason) {
        log.info("Cancelling commission {} with reason: {}", commissionId, reason);

        PartnerCommission commission = getCommissionById(commissionId);

        if (commission.getStatus() == CommissionStatus.PAID) {
            throw new BusinessException("messages.commission.alreadyPaid");
        }

        commission.setStatus(CommissionStatus.CANCELLED);
        commission.setNotes(commission.getNotes() + "\nCancellation reason: " + reason);

        partnerCommissionRepository.save(commission);
        log.info("Commission {} cancelled successfully", commissionId);
    }

    /**
     * Gets commission by ID.
     */
    @Transactional(readOnly = true)
    public PartnerCommission getCommissionById(Long id) {
        PartnerCommission commission = getCommissionEntityById(id);
        if (commission == null) {
            throw new BusinessException("messages.commission.notFound");
        }
        return commission;
    }

    /**
     * Lists commissions by partner with pagination.
     */
    @Transactional(readOnly = true)
    public Page<PartnerCommissionResponse> getCommissionsByPartner(Long partnerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "calculatedAt"));
        Page<PartnerCommission> commissions = partnerCommissionRepository.findByPartnerId(partnerId, pageable);
        return commissions.map(partnerCommissionMapper::toPartnerCommissionResponse);
    }

    /**
     * Lists commissions by status with pagination.
     */
    @Transactional(readOnly = true)
    public Page<PartnerCommissionResponse> getCommissionsByStatus(CommissionStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "calculatedAt"));
        Page<PartnerCommission> commissions = partnerCommissionRepository.findByStatus(status, pageable);
        return commissions.map(partnerCommissionMapper::toPartnerCommissionResponse);
    }

    /**
     * Lists commissions by partner and status.
     */
    @Transactional(readOnly = true)
    public List<PartnerCommissionResponse> getCommissionsByPartnerAndStatus(Long partnerId, CommissionStatus status) {
        List<PartnerCommission> commissions = partnerCommissionRepository.findByPartnerIdAndStatus(partnerId, status);
        return commissions.stream()
                .map(partnerCommissionMapper::toPartnerCommissionResponse)
                .toList();
    }

    /**
     * Gets commissions by contract ID.
     */
    @Transactional(readOnly = true)
    public List<PartnerCommissionResponse> getCommissionsByContractId(Long contractId) {
        List<PartnerCommission> commissions = partnerCommissionRepository.findByContractId(contractId);
        return commissions.stream()
                .map(partnerCommissionMapper::toPartnerCommissionResponse)
                .toList();
    }

    /**
     * Gets total commission amount for a partner by status.
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalCommissionByPartnerAndStatus(Long partnerId, CommissionStatus status) {
        BigDecimal total = partnerCommissionRepository.sumByPartnerIdAndStatus(partnerId, status);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Gets pending commission amount for a partner.
     */
    @Transactional(readOnly = true)
    public BigDecimal getPendingCommissionAmount(Long partnerId) {
        return getTotalCommissionByPartnerAndStatus(partnerId, CommissionStatus.PENDING);
    }

    /**
     * Gets paid commission amount for a partner.
     */
    @Transactional(readOnly = true)
    public BigDecimal getPaidCommissionAmount(Long partnerId) {
        return getTotalCommissionByPartnerAndStatus(partnerId, CommissionStatus.PAID);
    }

    /**
     * Gets commission summary for a partner.
     */
    @Transactional(readOnly = true)
    public PartnerCommissionSummary getCommissionSummary(Long partnerId) {
        BigDecimal pendingAmount = getPendingCommissionAmount(partnerId);
        BigDecimal paidAmount = getPaidCommissionAmount(partnerId);
        BigDecimal totalAmount = pendingAmount.add(paidAmount);

        long pendingCount = partnerCommissionRepository.countByPartnerIdAndStatus(partnerId, CommissionStatus.PENDING);
        long paidCount = partnerCommissionRepository.countByPartnerIdAndStatus(partnerId, CommissionStatus.PAID);

        return PartnerCommissionSummary.builder()
                .partnerId(partnerId)
                .pendingAmount(pendingAmount)
                .paidAmount(paidAmount)
                .totalAmount(totalAmount)
                .pendingCount(pendingCount)
                .paidCount(paidCount)
                .totalCount(pendingCount + paidCount)
                .build();
    }

    /**
     * Bulk approve commissions for a partner.
     */
    public void bulkApproveCommissions(Long partnerId, Long approvedByUserId) {
        log.info("Bulk approving commissions for partner {} by user {}", partnerId, approvedByUserId);

        List<PartnerCommission> pendingCommissions = partnerCommissionRepository.findByPartnerIdAndStatus(partnerId, CommissionStatus.PENDING);
        User approvedBy = getUserById(approvedByUserId);
        LocalDateTime now = LocalDateTime.now();

        for (PartnerCommission commission : pendingCommissions) {
            commission.setStatus(CommissionStatus.PAID);
            commission.setApprovedBy(approvedBy);
            commission.setPaidAt(now);
        }

        partnerCommissionRepository.saveAll(pendingCommissions);
        log.info("Bulk approved {} commissions for partner {}", pendingCommissions.size(), partnerId);
    }

    /**
     * Pays a commission (same as approve for this implementation).
     */
    public PartnerCommissionResponse payCommission(Long commissionId) {
        log.info("Paying commission {}", commissionId);

        PartnerCommission commission = getCommissionById(commissionId);

        if (commission.getStatus() != CommissionStatus.PENDING) {
            throw new BusinessException("messages.commission.notPending");
        }

        commission.setStatus(CommissionStatus.PAID);
        commission.setPaidAt(LocalDateTime.now());

        PartnerCommission savedCommission = partnerCommissionRepository.save(commission);
        log.info("Commission {} paid successfully", commissionId);

        return partnerCommissionMapper.toPartnerCommissionResponse(savedCommission);
    }

    // Private helper methods

    private void validateCommissionRequest(PartnerCommissionRequest request) {
        if (request.getPartnerId() == null) {
            throw new BusinessException("messages.commission.partnerRequired");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("messages.commission.invalidAmount");
        }
        if (request.getCommissionType() == null) {
            throw new BusinessException("messages.commission.typeRequired");
        }
    }

    private PartnerCommission buildCommissionFromRequest(PartnerCommissionRequest request, Partner partner) {
        PartnerCommission commission = partnerCommissionMapper.toPartnerCommission(request);
        commission.setPartner(partner);
        commission.setStatus(CommissionStatus.PENDING);
        commission.setCalculatedAt(LocalDateTime.now());

        // Set related entities if provided
        if (request.getContractId() != null) {
            Contract contract = getContractById(request.getContractId());
            commission.setContract(contract);
            commission.setCustomer(contract.getCustomer());
        }

        if (request.getPurchaseId() != null) {
            Purchase purchase = getPurchaseById(request.getPurchaseId());
            commission.setPurchase(purchase);
        }

        return commission;
    }

    private void calculateCommissionAmount(PartnerCommission commission, PartnerCommissionRequest request) {
        if (commission.getCommissionType() == CommissionType.REFERRAL_BONUS) {
            // For referral bonus, use the amount directly
            commission.setCommissionAmount(request.getAmount());
            commission.setBaseAmount(request.getAmount());
            commission.setCommissionPercentage(BigDecimal.valueOf(100));
        } else {
            // For other types, calculate percentage-based commission
            BigDecimal percentage = getCommissionPercentageForType(commission.getCommissionType());
            commission.setBaseAmount(request.getAmount());
            commission.setCommissionPercentage(percentage);
            calculateCommissionAmount(commission);
        }
    }

    private void calculateCommissionAmount(PartnerCommission commission) {
        BigDecimal amount = commission.getBaseAmount()
                .multiply(commission.getCommissionPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        commission.setCommissionAmount(amount);
    }

    private BigDecimal getCommissionPercentageForType(CommissionType type) {
        return switch (type) {
            case CUSTOMER_ACQUISITION -> BigDecimal.valueOf(3.0); // 3%
            case SALES_COMMISSION -> BigDecimal.valueOf(2.0); // 2%
            case PERFORMANCE_BONUS -> BigDecimal.valueOf(5.0); // 5%
            default -> BigDecimal.valueOf(1.0); // 1%
        };
    }

    private BigDecimal getDefaultSalesCommissionPercentage() {
        Optional<PartnerProfitCalculationConfig> config = configRepository.findByIsActiveTrue();
        // Assuming there's a sales commission percentage field in config
        return config.map(c -> BigDecimal.valueOf(2.0)) // Default 2%
                .orElse(BigDecimal.valueOf(2.0));
    }

    private boolean commissionExistsForCustomer(Long partnerId, Long customerId) {
        return partnerCommissionRepository.existsByPartnerIdAndCustomerIdAndCommissionType(
                partnerId, customerId, CommissionType.CUSTOMER_ACQUISITION);
    }

    private boolean commissionExistsForContract(Long partnerId, Long contractId) {
        return partnerCommissionRepository.existsByPartnerIdAndContractIdAndCommissionType(
                partnerId, contractId, CommissionType.SALES_COMMISSION);
    }

    private PartnerCustomerAcquisition getPartnerCustomerAcquisition(Long partnerId, Long customerId) {
        return partnerCustomerAcquisitionRepository.findByPartnerIdAndCustomerId(partnerId, customerId)
                .orElse(null);
    }

    // Entity retrieval helper methods

    private Partner getPartnerById(Long partnerId) {
        return partnerRepository.findById(partnerId)
                .orElseThrow(() -> new BusinessException("messages.partner.notFound"));
    }

    private Contract getContractById(Long contractId) {
        return contractRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException("messages.contract.notFound"));
    }

    private Purchase getPurchaseById(Long purchaseId) {
        return purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new BusinessException("messages.purchase.notFound"));
    }

    private Customer getCustomerById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException("messages.customer.notFound"));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("messages.user.notFound"));
    }



    private PartnerCommission getCommissionEntityById(Long id) {
        return partnerCommissionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("messages.commission.notFound"));
    }
}
