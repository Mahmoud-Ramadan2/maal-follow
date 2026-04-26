package com.mahmoud.maalflow.modules.installments.contract.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.exception.ObjectNotFoundException;
import com.mahmoud.maalflow.exception.UserNotFoundException;
import com.mahmoud.maalflow.modules.installments.contract.dto.ContractRequest;
import com.mahmoud.maalflow.modules.installments.contract.dto.ContractResponse;
import com.mahmoud.maalflow.modules.installments.contract.dto.ContractMetadataUpdateRequest;
import com.mahmoud.maalflow.modules.installments.contract.entity.Contract;
import com.mahmoud.maalflow.modules.installments.contract.mapper.ContractMapper;
import com.mahmoud.maalflow.modules.installments.contract.repo.ContractRepository;
import com.mahmoud.maalflow.modules.installments.customer.entity.Customer;
import com.mahmoud.maalflow.modules.installments.customer.repo.CustomerRepository;
import com.mahmoud.maalflow.modules.installments.partner.entity.Partner;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerRepository;
import com.mahmoud.maalflow.modules.installments.purchase.entity.Purchase;
import com.mahmoud.maalflow.modules.installments.purchase.repo.PurchaseRepository;
import com.mahmoud.maalflow.modules.installments.contract.enums.ContractStatus;
import com.mahmoud.maalflow.modules.installments.schedule.service.InstallmentScheduleService;
import com.mahmoud.maalflow.modules.installments.capital.service.CapitalService;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import com.mahmoud.maalflow.modules.shared.user.repo.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

import static com.mahmoud.maalflow.modules.shared.constants.AppConstants.MIN_PURCHASE_PRICE;
import static com.mahmoud.maalflow.modules.shared.constants.AppConstants.SYSTEM_USER_ID;

@Service
@AllArgsConstructor
@Slf4j
public class ContractService {

    private final CustomerRepository customerRepository;
    private final PurchaseRepository purchaseRepository;
    private final ContractRepository contractRepository;
    private final PartnerRepository partnerRepository;
    private final UserRepository userRepository;
    private final ContractMapper contractMapper;
    private final InstallmentScheduleService installmentScheduleService;
    private final ContractFinancialValidator contractFinancialValidator;
    private final ContractPricingPolicy contractPricingPolicy;
    private final ContractTermCalculator contractTermCalculator;
    private final ContractDefaultsApplier contractDefaultsApplier;
    private final ContractStatusPolicy contractStatusPolicy;
    private final CapitalService capitalService;
    //private final MessageSource messageSource;

    @Transactional
    public ContractResponse create(ContractRequest request, Long customerId) {

        //

        Long finalCustomerId = (customerId != null) ? customerId : request.getCustomerId();
        // Check if there is an active contract for this purchase
        if (contractRepository.existsByPurchaseIdAndStatusAndCustomerId(
                request.getPurchaseId(), ContractStatus.ACTIVE, finalCustomerId)) {
            log.warn("Rejecting duplicate active contract creation: customerId={}, purchaseId={}",
                    finalCustomerId, request.getPurchaseId());
            throw new BusinessException("messages.contract.alreadyExists");
        }

        Customer customer = customerRepository.findByIdAndActiveTrue(finalCustomerId).
                orElseThrow(() -> new UserNotFoundException("messages.customer.notFound", finalCustomerId));
        Purchase purchase = purchaseRepository.findById(request.getPurchaseId()).
                orElseThrow(() -> new ObjectNotFoundException("messages.purchase.notFound", request.getPurchaseId()));

        Contract contract = contractMapper.toContract(request);
        contract.setPurchase(purchase);
        contract.setCustomer(customer);
        User systemUser = resolveSystemUser();
        if (request.getResponsibleUserId() == null) {
            contract.setResponsibleUser(systemUser);
        } else {
            User responsibleUser = userRepository.findById(request.getResponsibleUserId())
                    .orElseThrow(() -> new UserNotFoundException("messages.user.notFound", request.getResponsibleUserId()));
            contract.setResponsibleUser(responsibleUser);
        }
        // TODO auto Set createdBy, updatedBy from Security
        // Temporary hardcoded user with ID 1
        contract.setCreatedBy(systemUser);
        contract.setUpdatedBy(systemUser);

        // Set partner if provided
        if (request.getPartnerId() != null) {
            Partner partner = partnerRepository.findById(request.getPartnerId())
                    .orElseThrow(() -> new ObjectNotFoundException("messages.partner.notFound", request.getPartnerId()));
            contract.setPartner(partner);
        }
        if (request.getStatus() != null) {
            contract.setStatus(ContractStatus.ACTIVE);
//            contract.setStatus(request.getStatus());
        } else {
            contract.setStatus(ContractStatus.ACTIVE);
        }

        // Apply default values
        contractDefaultsApplier.applyDefaults(contract);

        if (purchase.getBuyPrice() == null || purchase.getBuyPrice().compareTo(MIN_PURCHASE_PRICE) < 0) {
            throw new BusinessException("messages.buyPrice.invalid");
        }

        // Auto calculate original price = purchase price + additional costs
        BigDecimal originalPrice = purchase.getBuyPrice().add(contract.getAdditionalCosts());
        contract.setOriginalPrice(originalPrice);

        if (request.getMonths() != null && request.getMonths() > 0 && request.getMonths() <=60) {
            contract.setMonths(request.getMonths());
        } else{
            // temp null
            contract.setMonths(null);
        }
        // Auto Calculate final price if not provided depending on Months and original price
        contractPricingPolicy.applyFinalPrice(contract, contract.getFinalPrice(), contract.getOriginalPrice());


        // Validate financials
        contractFinancialValidator.validateFinancials(contract.getFinalPrice(),
               contract.getDownPayment() ,
               originalPrice,
               contract.getAgreedPaymentDay());

        if (contract.getOriginalPrice().compareTo(contract.getFinalPrice()) == 0) {
            // TODO: Handle this case (one shoot payment contract)
        }


        // Auto calculate the remaining amount
        BigDecimal remaining = contract.getFinalPrice().subtract(contract.getDownPayment());
        contract.setRemainingAmount(remaining);

        // Calculate months and monthlyAmount based on user input
        contractTermCalculator.calculateMonthsAndAmount(contract, remaining, contract.getMonths(), request.getMonthlyAmount());

        // Calculate profit
        BigDecimal profitAmount = contractPricingPolicy.calculateProfit(contract);
        contract.setProfitAmount(profitAmount);
       // Default net profit = profit amount initially
       contract.setNetProfit(contract.getProfitAmount());

        // save contract
        Contract savedContract = contractRepository.save(contract);

        // Allocate capital: financed principal = (originalPrice - downPayment)
        BigDecimal financedPrincipal = contract.getOriginalPrice().subtract(contract.getDownPayment());
        User currentUser = resolveSystemUser();
        if (financedPrincipal.compareTo(BigDecimal.ZERO) > 0 && currentUser != null) {
            capitalService.allocateCapitalForContract(savedContract, financedPrincipal, currentUser);
            savedContract = contractRepository.save(savedContract); // Refresh to get capitalAllocated
        }

        // generate contract schedules
        if (savedContract.getStatus().equals(ContractStatus.ACTIVE)) {
            installmentScheduleService.generateSchedulesForContract(savedContract.getId());
        }

        log.info("Contract created: contractId={}, customerId={}, purchaseId={}, status={}, capitalAllocated={}",
                savedContract.getId(), finalCustomerId, request.getPurchaseId(), savedContract.getStatus(), 
                savedContract.getCapitalAllocated());


        return contractMapper.toContractResponse(savedContract);
    }



    @Transactional
    public ContractResponse update(Long id, ContractRequest request) {
        // TODO auto get current user from Security
        Contract existingContract = contractRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("messages.contract.notFound", id));

        boolean hasPaidSchedules = installmentScheduleService.existsPaidByContractId(id);

        if (hasPaidSchedules) {
            throw new BusinessException("messages.contract.cannotModifyAfterPayments");
        }

        // Only allow metadata edits after contract activation (if preferred)
        // For now, allow full edits before payment activity
        
        if (request.getResponsibleUserId() != null) {
            User responsibleUser = userRepository.findById(request.getResponsibleUserId())
                    .orElseThrow(() -> new UserNotFoundException("messages.user.notFound", request.getResponsibleUserId()));
            existingContract.setResponsibleUser(responsibleUser);
        }
        // TODO: auto get current user from Security
        existingContract.setUpdatedBy(resolveSystemUser());

        // user can't change customer or purchase
//        if (request.getCustomerId() != null && !existingContract.getCustomer().getId().equals(request.getCustomerId())) {
//            Customer customer = customerRepository.findByIdAndActiveTrue(request.getCustomerId()).
//                    orElseThrow(() -> new UserNotFoundException("messages.customer.notFound", request.getCustomerId()));
//            existingContract.setCustomer(customer);
//        }
//        if (request.getPurchaseId() != null && !existingContract.getPurchase().getId().equals(request.getPurchaseId())) {
//           Purchase purchase = purchaseRepository.findById(request.getPurchaseId()).
//                    orElseThrow(() -> new ObjectNotFoundException("messages.purchase.notFound", request.getPurchaseId()));
//            existingContract.setPurchase(purchase);
//        }
//        // TODO: check for unpaid installments before changing purchase

        if(request.getStatus() != null && !request.getStatus().equals(existingContract.getStatus())) {
            contractStatusPolicy.validateStatusTransition(existingContract.getStatus(), request.getStatus(), existingContract);
            existingContract.setStatus(request.getStatus());
            if(request.getStatus().equals(ContractStatus.COMPLETED)) {
                log.info("Contract {} marked as completed during update", existingContract.getId());
            }
        }

        if (request.getPartnerId() != null) {
            Partner partner = partnerRepository.findById(request.getPartnerId())
                    .orElseThrow(() -> new ObjectNotFoundException("messages.partner.notFound", request.getPartnerId()));
            existingContract.setPartner(partner);
        }

        if (request.getAdditionalCosts() != null && !request.getAdditionalCosts().equals(existingContract.getAdditionalCosts())) {
            existingContract.setAdditionalCosts(request.getAdditionalCosts());
        }

        if (request.getDownPayment() != null && request.getDownPayment().compareTo(BigDecimal.ZERO) >= 0 ) {
            existingContract.setDownPayment(request.getDownPayment());
        }

        if (request.getMonths() != null && request.getMonths() > 0 && request.getMonths() <=60) {
            existingContract.setMonths(request.getMonths());
        }

        if (request.getFinalPrice() != null && !request.getFinalPrice().equals(existingContract.getFinalPrice())) {
            existingContract.setFinalPrice(request.getFinalPrice());
        }

        if (request.getAgreedPaymentDay() != null && !request.getAgreedPaymentDay().equals(existingContract.getAgreedPaymentDay())) {
            existingContract.setAgreedPaymentDay(request.getAgreedPaymentDay());
        }
        if (request.getStartDate() != null && !request.getStartDate().equals(existingContract.getStartDate())) {
            existingContract.setStartDate(request.getStartDate());
        }
        if (request.getNotes() != null && !request.getNotes().equals(existingContract.getNotes())) {
            existingContract.setNotes(request.getNotes());
        }
        if (request.getEarlyPaymentDiscountRate() != null && request.getEarlyPaymentDiscountRate().compareTo(BigDecimal.ZERO) >= 0) {
            existingContract.setEarlyPaymentDiscountRate(request.getEarlyPaymentDiscountRate());
        }

        if (request.getCashDiscountRate() != null && request.getCashDiscountRate().compareTo(BigDecimal.ZERO) >= 0) {
            existingContract.setCashDiscountRate(request.getCashDiscountRate());
        }
        if (request.getMonthlyAmount() != null && request.getMonthlyAmount().compareTo(BigDecimal.ZERO )>= 0 ) {
            existingContract.setMonthlyAmount(request.getMonthlyAmount());
        }

        contractDefaultsApplier.applyDefaults(existingContract);

        if (existingContract.getPurchase().getBuyPrice() == null || existingContract.getPurchase().getBuyPrice().compareTo(MIN_PURCHASE_PRICE) < 0) {
            throw new BusinessException("messages.buyPrice.invalid");
        }

        // Auto calculate original price = purchase price + additional costs
        BigDecimal originalPrice = existingContract.getPurchase().getBuyPrice().add(existingContract.getAdditionalCosts());
        existingContract.setOriginalPrice(originalPrice);

        // Auto Calculate final price if not provided depending on Months and original price
        contractPricingPolicy.applyFinalPrice(existingContract, existingContract.getFinalPrice(), existingContract.getOriginalPrice());


        contractFinancialValidator.validateFinancials(existingContract.getFinalPrice(),
                existingContract.getDownPayment(),
                originalPrice,
                existingContract.getAgreedPaymentDay());

        if (existingContract.getOriginalPrice().compareTo(existingContract.getFinalPrice()) == 0) {
            // TODO: Handle this case (one shoot payment contract)
        }
        // Auto calculate the remaining amount
        BigDecimal remaining = existingContract.getFinalPrice().subtract(existingContract.getDownPayment());
        existingContract.setRemainingAmount(remaining);

        contractTermCalculator.calculateMonthsAndAmount(existingContract, remaining, existingContract.getMonths(), existingContract.getMonthlyAmount());

//        Integer effectiveMonths = request.getMonths() != null ? request.getMonths() : existingContract.getMonths();
//        BigDecimal effectiveMonthlyAmount = request.getMonthlyAmount() != null ? request.getMonthlyAmount() : existingContract.getMonthlyAmount();
//        calculateMonthsAndAmount(existingContract, remaining, effectiveMonths, effectiveMonthlyAmount);
//




        // Calculate profit
        existingContract.setProfitAmount(contractPricingPolicy.calculateProfit(existingContract));

        // save contract
        Contract updatedContract = contractRepository.save(existingContract);

        // generate contract schedules
        if (updatedContract.getStatus().equals(ContractStatus.ACTIVE) || updatedContract.getStatus().equals(ContractStatus.LATE)) {
            installmentScheduleService.generateSchedulesForContract(updatedContract.getId());
        }

        log.info("Contract updated: contractId={}, customerId={}, purchaseId={}, status={}",
                existingContract.getId(),
                existingContract.getCustomer() != null ? existingContract.getCustomer().getId() : null,
                existingContract.getPurchase() != null ? existingContract.getPurchase().getId() : null,
                existingContract.getStatus());

        return contractMapper.toContractResponse(updatedContract);
    }

    /**
     * Update non-financial metadata only (notes, responsible user, partner).
     * Callable even after payment activity.
     */
    @Transactional
    public ContractResponse updateMetadata(Long id, ContractMetadataUpdateRequest request) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("messages.contract.notFound", id));

        if (request.getNotes() != null) {
            contract.setNotes(request.getNotes());
        }

        if (Boolean.TRUE.equals(request.getClearResponsibleUser())) {
            contract.setResponsibleUser(null);
        } else if (request.getResponsibleUserId() != null) {
            User responsibleUser = userRepository.findById(request.getResponsibleUserId())
                    .orElseThrow(() -> new UserNotFoundException("messages.user.notFound", request.getResponsibleUserId()));
            contract.setResponsibleUser(responsibleUser);
        }

        if (Boolean.TRUE.equals(request.getClearPartner())) {
            contract.setPartner(null);
        } else if (request.getPartnerId() != null) {
            Partner partner = partnerRepository.findById(request.getPartnerId())
                    .orElseThrow(() -> new ObjectNotFoundException("messages.partner.notFound", request.getPartnerId()));
            contract.setPartner(partner);
        }

        contract.setUpdatedBy(resolveSystemUser());
        Contract saved = contractRepository.save(contract);

        log.info("Updated contract {} metadata only", id);
        return contractMapper.toContractResponse(saved);
    }


    /**
     * Apply early payment discount
     * Requirement #7: Handle early payment discounts
     */
    public BigDecimal calculateEarlyPaymentDiscount(Long contractId, BigDecimal remainingAmount) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ObjectNotFoundException("messages.contract.notFound", contractId));

        if (contract.getEarlyPaymentDiscountRate() != null &&
                contract.getEarlyPaymentDiscountRate().compareTo(BigDecimal.ZERO) > 0) {
            return remainingAmount.multiply(contract.getEarlyPaymentDiscountRate())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Apply cash payment discount
     * Requirement #7: Handle cash payment discounts
     */
    public BigDecimal calculateCashDiscount(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ObjectNotFoundException("messages.contract.notFound", contractId));

        if (contract.getCashDiscountRate() != null &&
                contract.getCashDiscountRate().compareTo(BigDecimal.ZERO) > 0) {
            return contract.getFinalPrice().multiply(contract.getCashDiscountRate())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    // Get contracts for a specific customer with pagination
    public Page<ContractResponse> getCustomerWithContracts(Long customerId, int page, int size) {
        boolean customerExists = customerRepository.existsByIdAndActiveTrue(customerId);
        if (!customerExists) {
            throw new UserNotFoundException("messages.customer.notFound", customerId);
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return contractRepository.findByCustomerId(customerId, pageable);
    }

    /**
     * Get all contracts for a customer including linked accounts
     */
    public List<ContractResponse> getAllContractsForLinkedCustomers(Long customerId) {
        List<Contract> contracts = contractRepository.findAllContractsByLinkedCustomers(customerId);
        return contracts.stream()
                .map(contractMapper::toContractResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get contracts by payment day for collection route
     */
    public List<ContractResponse> getContractsByPaymentDay(Integer paymentDay) {
        List<Contract> contracts = contractRepository.findActiveContractsByAgreedPaymentDay(paymentDay);
        return contracts.stream()
                .map(contractMapper::toContractResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get contracts by customer address for collection route
     * Requirement #6: Sort by address for collection
     */
    public List<ContractResponse> getContractsByAddress(String address) {
        List<Contract> contracts = contractRepository.findActiveContractsByCustomerAddress(address);
        return contracts.stream()
                .map(contractMapper::toContractResponse)
                .collect(Collectors.toList());
    }

    public ContractResponse getById(Long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("messages.contract.notFound", id));
        return contractMapper.toContractResponse(contract);
    }

    public ContractResponse getByContractNumber(String contractNumber) {
        Contract contract = contractRepository.findByContractNumber(contractNumber)
                .orElseThrow(() -> new ObjectNotFoundException("messages.contract.notFound.byNumber", contractNumber));
        return contractMapper.toContractResponse(contract);
    }

    public Page<ContractResponse> getContractsByStatus(ContractStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Contract> contracts = contractRepository.findByStatus(status, pageable);
        if (status == ContractStatus.ACTIVE) {
            return contracts.map(contractMapper::toContractResponse);
        }
        // TODO CHECk ADMIN
        if (status == ContractStatus.ALL) {
            Page<Contract> allContracts = contractRepository.findAll(pageable);
            return allContracts.map(contractMapper::toContractResponse);
        }
        return contracts.map(contractMapper::toContractResponse);
    }

    /**
     * Get total monthly expected installments
     * Requirement #12: Calculate expected monthly collections
     */
    public BigDecimal getTotalMonthlyExpected() {
        return contractRepository.getTotalMonthlyExpectedAmount();
    }

    /**
     * Get total net profit from all contracts
     * Requirement #9: Calculate total profits for distribution
     */
    public BigDecimal getTotalNetProfit() {
        return contractRepository.getTotalNetProfit();
    }




    /**
     * Mark contract as completed
     * Requirement #13: Remove completed contracts from monthly calculations
     */
    @Transactional
    public ContractResponse markAsCompleted(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ObjectNotFoundException("messages.contract.notFound", contractId));

        contractStatusPolicy.validateStatusTransition(contract.getStatus(), ContractStatus.COMPLETED, contract);
        log.info("Contract {} marked as completed", contract.getId());
        return contractMapper.toContractResponse(contractRepository.save(contract));
    }

    /**
     * Check if a contract is completed
     */
    public boolean isContractCompleted(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ObjectNotFoundException("messages.contract.notFound", contractId));

        return contract.getStatus() == ContractStatus.COMPLETED;

    }


    // ============== Helper Methods ==============


    private User resolveSystemUser() {
        return userRepository.findById(SYSTEM_USER_ID).orElse(null);
    }


}