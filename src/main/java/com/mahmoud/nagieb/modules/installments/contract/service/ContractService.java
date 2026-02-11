package com.mahmoud.nagieb.modules.installments.contract.service;

import com.mahmoud.nagieb.exception.BusinessException;
import com.mahmoud.nagieb.exception.ObjectNotFoundException;
import com.mahmoud.nagieb.exception.UserNotFoundException;
import com.mahmoud.nagieb.modules.installments.contract.dto.ContractRequest;
import com.mahmoud.nagieb.modules.installments.contract.dto.ContractResponse;
import com.mahmoud.nagieb.modules.installments.contract.entity.Contract;
import com.mahmoud.nagieb.modules.installments.contract.mapper.ContractMapper;
import com.mahmoud.nagieb.modules.installments.contract.repo.ContractRepository;
import com.mahmoud.nagieb.modules.installments.customer.entity.Customer;
import com.mahmoud.nagieb.modules.installments.customer.repo.CustomerRepository;
import com.mahmoud.nagieb.modules.installments.partner.entity.Partner;
import com.mahmoud.nagieb.modules.installments.partner.repo.PartnerRepository;
import com.mahmoud.nagieb.modules.installments.purchase.entity.Purchase;
import com.mahmoud.nagieb.modules.installments.purchase.repo.PurchaseRepository;
import com.mahmoud.nagieb.modules.installments.contract.enums.ContractStatus;
import com.mahmoud.nagieb.modules.shared.user.entity.User;
import com.mahmoud.nagieb.modules.shared.user.repo.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
    //private final MessageSource messageSource;

    @Transactional
    public ContractResponse create(ContractRequest request) {

        // Check if there is an active contract for this purchase
        if (contractRepository.existsByPurchaseIdAndStatusAndCustomerId(
                request.getPurchaseId(), ContractStatus.ACTIVE, request.getCustomerId())) {
            log.error("Attempt to create duplicate active contract for purchase ID: {}", request.getPurchaseId());
            throw new BusinessException("messages.contract.alreadyExists");
        }

        Customer customer = customerRepository.findByIdAndActiveTrue(request.getCustomerId()).
                orElseThrow(() -> new UserNotFoundException("messages.customer.notFound", request.getCustomerId()));
        Purchase purchase = purchaseRepository.findById(request.getPurchaseId()).
                orElseThrow(() -> new ObjectNotFoundException("messages.purchase.notFound", request.getPurchaseId()));

        Contract contract = contractMapper.toContract(request);
        contract.setPurchase(purchase);
        contract.setCustomer(customer);
        if( request.getResponsibleUserId() == null ) {
            contract.setResponsibleUser(userRepository.findById(1L).orElse(null));
        }
        else {
            User responsibleUser = userRepository.findById(request.getResponsibleUserId())
                    .orElseThrow(() -> new UserNotFoundException("messages.user.notFound", request.getResponsibleUserId()));
            contract.setResponsibleUser(responsibleUser);
        }
        // TODO auto Set createdBy, updatedBy from Security
         // Temporary hardcoded user with ID 1
        contract.setCreatedBy(userRepository.findById(1L).orElse(null));
       contract.setUpdatedBy(userRepository.findById(1L).orElse(null));

        // Set partner if provided
        if (request.getPartnerId() != null) {
            Partner partner = partnerRepository.findById(request.getPartnerId())
                    .orElseThrow(() -> new ObjectNotFoundException("messages.partner.notFound", request.getPartnerId()));
            contract.setPartner(partner);
        }
        if (request.getStatus() != null) {
            contract.setStatus(request.getStatus());
        } else {
            contract.setStatus(ContractStatus.ACTIVE);
        }

        // Apply default values
        applyDefaults(contract);
        // Auto calculate original price = purchase price + additional costs
        BigDecimal originalPrice = purchase.getBuyPrice().add(contract.getAdditionalCosts());
        contract.setOriginalPrice(originalPrice);

// Validate financials
       validateFinancials(contract.getFinalPrice(),
               request.getDownPayment(),
               originalPrice,
               request.getMonths(),
               contract.getAgreedPaymentDay());
        if (request.getDownPayment().compareTo(request.getFinalPrice()) == 0) {
            // TODO: Handle this case (one shoot payment contract)
        }

        // Auto Calculate final price if not provided depending Months and purchase price
        if (request.getFinalPrice() != null) {
            setAndValidateFinalPrice(contract, request.getFinalPrice(), purchase.getBuyPrice());
        } else {
            setAndValidateFinalPrice(contract, null, purchase.getBuyPrice());
        }

        // Auto calculate the remaining amount
        BigDecimal remaining = request.getFinalPrice().subtract(request.getDownPayment());
        contract.setRemainingAmount(remaining);

        // Calculate months and monthlyAmount based on user input
        calculateMonthsAndAmount(contract, remaining, request.getMonths(), request.getMonthlyAmount());



        // Calculate profit
       contract.setProfitAmount(calculateProfit(contract));
       // Default net profit = profit amount initially
       contract.setNetProfit(contract.getProfitAmount());

       // save contract
        Contract savedContract = contractRepository.save(contract);
       // generate contract schedules
        if(contract.getStatus().equals(ContractStatus.ACTIVE)) {
            installmentScheduleService.generateSchedulesForContract(savedContract.getId());
        }

        log.info("Creating contract with its schedules for customer ID: {} and purchase ID: {}", request.getCustomerId(), request.getPurchaseId());


        return contractMapper.toContractResponse(savedContract);
    }



    @Transactional
    public ContractResponse update(Long id, ContractRequest request) {
        // TODO auto get current user from Security
        Contract existingContract = contractRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("messages.contract.notFound", id));

        if (request.getResponsibleUserId() != null) {
            User responsibleUser = userRepository.findById(request.getResponsibleUserId())
                    .orElseThrow(() -> new UserNotFoundException("messages.user.notFound", request.getResponsibleUserId()));
            existingContract.setResponsibleUser(responsibleUser);
        }
    // TODO: auto get current user from Security
      existingContract.setUpdatedBy(userRepository.findById(1L).orElse(null));

        if (request.getCustomerId() != null && !existingContract.getCustomer().getId().equals(request.getCustomerId())) {
            Customer customer = customerRepository.findByIdAndActiveTrue(request.getCustomerId()).
                    orElseThrow(() -> new UserNotFoundException("messages.customer.notFound", request.getCustomerId()));
            existingContract.setCustomer(customer);
        }
        if (request.getPurchaseId() != null && !existingContract.getPurchase().getId().equals(request.getPurchaseId())) {
           Purchase purchase = purchaseRepository.findById(request.getPurchaseId()).
                    orElseThrow(() -> new ObjectNotFoundException("messages.purchase.notFound", request.getPurchaseId()));
            existingContract.setPurchase(purchase);
        }
        // TODO: check for unpaid installments before changing purchase
        if(request.getStatus() != null ) {
            existingContract.setStatus(request.getStatus());
        }

        if (request.getPartnerId() != null) {
            Partner partner = partnerRepository.findById(request.getPartnerId())
                    .orElseThrow(() -> new ObjectNotFoundException("messages.partner.notFound", request.getPartnerId()));
            existingContract.setPartner(partner);
        }
        // Recalculate remaining amount if finalPrice or downPayment changed
        if (request.getFinalPrice() != null || request.getDownPayment() != null) {
            BigDecimal remaining = existingContract.getFinalPrice().subtract(existingContract.getDownPayment());
            existingContract.setRemainingAmount(remaining);

            // Recalculate months and monthlyAmount
            calculateMonthsAndAmount(existingContract, remaining, request.getMonths(), request.getMonthlyAmount());
        } else if (request.getMonths() != null || request.getMonthlyAmount() != null) {
            // Only months or monthlyAmount changed, recalculate
            BigDecimal remaining = existingContract.getRemainingAmount();
            calculateMonthsAndAmount(existingContract, remaining, request.getMonths(), request.getMonthlyAmount());
        }
        if (request.getAdditionalCosts() != null) {
            existingContract.setAdditionalCosts(request.getAdditionalCosts());
        }
        // Auto calculate original price = purchase price + additional costs
        BigDecimal originalPrice = existingContract.getPurchase().getBuyPrice().add(existingContract.getAdditionalCosts());
        existingContract.setOriginalPrice(originalPrice);
        if (request.getAgreedPaymentDay() != null) {
            existingContract.setAgreedPaymentDay(request.getAgreedPaymentDay());
        }

        // Auto Calculate final price if not provided depending Months and purchase price
        if( request.getFinalPrice() != null ) {
            setAndValidateFinalPrice(existingContract, request.getFinalPrice(), existingContract.getPurchase().getBuyPrice());
        }


        // Validate financials
        validateFinancials(existingContract.getFinalPrice(),
                existingContract.getDownPayment(),
                originalPrice,
                existingContract.getMonths(),
                existingContract.getAgreedPaymentDay());


        if (existingContract.getDownPayment().compareTo(existingContract.getFinalPrice()) == 0) {
            // TODO: Handle this case (one shoot payment contract)
        }


        if (request.getStartDate() != null) {
            existingContract.setStartDate(request.getStartDate());
        }
        if (request.getNotes() != null) {
            existingContract.setNotes(request.getNotes());
        }


        // Calculate profit
        existingContract.setProfitAmount(calculateProfit(existingContract));

        if (request.getEarlyPaymentDiscountRate() != null) {
            existingContract.setEarlyPaymentDiscountRate(request.getEarlyPaymentDiscountRate());
        }

        return contractMapper.toContractResponse(contractRepository.save(existingContract));
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
         // TODO: Add checks to ensure all installments are paid before marking as completed

        contract.setStatus(ContractStatus.COMPLETED);
        contract.setCompletionDate(LocalDate.now());

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



    /**
     * Validate financial fields of the contract
     */
    private void validateFinancials(
            BigDecimal finalPrice,
            BigDecimal downPayment,
            BigDecimal originalPrice,
            Integer months,
            Integer agreedPaymentDay
    ) {
        if (downPayment.compareTo(finalPrice) > 0) {
            throw new BusinessException("messages.contract.downPayment.invalid");
        }

        if (finalPrice.compareTo(originalPrice) < 0) {
            throw new BusinessException("messages.contract.finalPrice.lessThanOrignalPrice");
        }

        if (months <= 0) {
            throw new BusinessException("messages.contract.months.invalid");
        }

        if (agreedPaymentDay < 1 || agreedPaymentDay > 31) {
            throw new BusinessException("messages.contract.agreedPaymentDay.invalid");

        }
    }

    /**
     * Apply default values to contract fields if not provided
     */
    private void applyDefaults(Contract contract) {
        if (contract.getAdditionalCosts() == null)
            contract.setAdditionalCosts(BigDecimal.ZERO);

        if (contract.getAgreedPaymentDay() == null)
            contract.setAgreedPaymentDay(1);

        if (contract.getCashDiscountRate() == null)
            contract.setCashDiscountRate(BigDecimal.ZERO);

        if (contract.getEarlyPaymentDiscountRate() == null)
            contract.setEarlyPaymentDiscountRate(BigDecimal.ZERO);

    }

    /**
     * Auto Calculate final price if not provided depending Months and purchase price
     */
    private void setAndValidateFinalPrice(Contract contract, BigDecimal finalPrice, BigDecimal purchasePrice) {

        // TODO validate final price business logic

        if (finalPrice == null) {
            finalPrice = BigDecimal.ZERO;
        }

        if (purchasePrice == null) {
            purchasePrice = BigDecimal.ZERO;
        }

        // Auto Calculate final price if not provided depending Months and purchase price
        if(finalPrice.compareTo(BigDecimal.ZERO) > 0 ) {
            // The minimum final price should be at least 10% markup over purchase price
            if (finalPrice.compareTo(purchasePrice) < 0) {
                throw new BusinessException("messages.contract.finalPrice.lessThanPurchasePrice");
            }
             if (finalPrice.compareTo(purchasePrice.
                    multiply(BigDecimal.valueOf(1.1))) < 0) {
                throw new BusinessException("messages.contract.finalPrice.lessThan10PercentMarkup");
            }
            else {
                 contract.setFinalPrice(finalPrice.setScale(2, RoundingMode.HALF_UP));
                return;
            }

        }

        BigDecimal multiplier;
        if (contract.getMonths() <= 6) {
            // 30% markup for up to 6 months
            multiplier = BigDecimal.valueOf(1.3);
        } else if (contract.getMonths() <= 12) {
            // 40% markup for 7 to 12 months
            multiplier = BigDecimal.valueOf(1.4);
        } else {
            // 50% markup for more than 12 months
            multiplier = BigDecimal.valueOf(1.5);
        }
        BigDecimal computed = purchasePrice.multiply(multiplier)
                .setScale(2, RoundingMode.HALF_UP);

        contract.setFinalPrice(computed);
    }

    /**
     * Calculate profit amount based on purchase price and final price
     * Requirement #15: Calculate original price + markup + additional costs
     */
    private BigDecimal calculateProfit(Contract contract) {
        // TODO validate profit calculation business logic
        // Profit = Final Price - Original Price (which includes purchase + additional costs)
        return contract.getFinalPrice().subtract(contract.getOriginalPrice());
    }

    /**
     * Calculate months and monthlyAmount based on user input.
     * User can provide either months OR monthlyAmount, and the other will be calculated.
     * If both are provided, validate they match.
     * If neither is provided, throw an error.
     */
    private void calculateMonthsAndAmount(Contract contract, BigDecimal remainingAmount, Integer months, BigDecimal monthlyAmount) {
        // Case 1: Both months and monthlyAmount provided - validate they match
        if (months != null && monthlyAmount != null) {
            BigDecimal calculatedTotal = monthlyAmount.multiply(BigDecimal.valueOf(months));
            BigDecimal difference = calculatedTotal.subtract(remainingAmount).abs();

            // Allow small difference due to rounding (within 1% or 10 units)
          //  BigDecimal tolerance = remainingAmount.multiply(BigDecimal.valueOf(0.01)).max(BigDecimal.TEN);

//            if (difference.compareTo(tolerance) > 0) {
            if (difference.compareTo(BigDecimal.ZERO) > 0) {
                log.error("Months and monthlyAmount mismatch: months={}, monthlyAmount={}, total={}, remaining={}",
                    months, monthlyAmount, calculatedTotal, remainingAmount);
                throw new BusinessException("messages.contract.monthsAmountMismatch");
            }

            contract.setMonths(months);
            contract.setMonthlyAmount(monthlyAmount);
            return;
        }

        // Case 2: Only monthlyAmount provided - calculate months
        if (monthlyAmount != null) {
            if (monthlyAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("messages.contract.invalidMonthlyAmount");
            }
            if (monthlyAmount.compareTo(remainingAmount) > 0) {
                throw new BusinessException("messages.contract.monthlyAmountExceedsTotal");
            }

            int calculatedMonths = remainingAmount.divide(monthlyAmount, 0, RoundingMode.UP).intValue();
            contract.setMonths(calculatedMonths);
            contract.setMonthlyAmount(monthlyAmount);
            log.info("Calculated months from monthlyAmount: monthlyAmount={}, calculatedMonths={}", monthlyAmount, calculatedMonths);
            return;
        }

        // Case 3: Only months provided - calculate monthlyAmount
        if (months != null) {
            if (months <= 0) {
                throw new BusinessException("messages.contract.invalidMonths");
            }

            BigDecimal calculatedAmount = remainingAmount.divide(
                BigDecimal.valueOf(months),
                2,
                RoundingMode.HALF_UP
            );

            contract.setMonths(months);
            contract.setMonthlyAmount(calculatedAmount);
            log.info("Calculated monthlyAmount from months: months={}, calculatedAmount={}", months, calculatedAmount);
            return;
        }

        // Case 4: Neither provided - error
        throw new BusinessException("messages.contract.monthsOrAmountRequired");
    }

}