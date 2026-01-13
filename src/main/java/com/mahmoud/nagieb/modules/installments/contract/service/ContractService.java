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
import com.mahmoud.nagieb.modules.installments.purchase.entity.ProductPurchase;
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
        ProductPurchase purchase = purchaseRepository.findById(request.getPurchaseId()).
                orElseThrow(() -> new ObjectNotFoundException("messages.purchase.notFound", request.getPurchaseId()));

        Contract contract = contractMapper.toContract(request);
        contract.setProductPurchase(purchase);
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

        // Apply default values
        applyDefaults(contract);
        // Auto calculate original price = purchase price + additional costs
        BigDecimal originalPrice = purchase.getBuyPrice().add(contract.getAdditionalCosts());
        contract.setOriginalPrice(originalPrice);

// Validate financials
       validateFinancials(request.getFinalPrice(),
               request.getDownPayment(),
               originalPrice,
               request.getMonths(),
               contract.getAgreedPaymentDay());
        if (request.getDownPayment().compareTo(request.getFinalPrice()) == 0) {
            // TODO: Handle this case (one shoot payment contract)
        }
        // Auto calculate the remaining amount and monthly amount
        BigDecimal remaining = request.getFinalPrice().subtract(request.getDownPayment());
        BigDecimal monthly = remaining.divide(
                BigDecimal.valueOf(request.getMonths()),
                2,
                RoundingMode.HALF_UP
        );

        contract.setRemainingAmount(remaining);
        contract.setMonthlyAmount(monthly);



        // Calculate profit
       contract.setProfitAmount(calculateProfit(contract));
       // Default net profit = profit amount initially
       contract.setNetProfit(contract.getProfitAmount());

        log.info("Creating contract for customer ID: {} and purchase ID: {}", request.getCustomerId(), request.getPurchaseId());
        return contractMapper.toContractResponse(contractRepository.save(contract));
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
        if (request.getPurchaseId() != null && !existingContract.getProductPurchase().getId().equals(request.getPurchaseId())) {
            ProductPurchase purchase = purchaseRepository.findById(request.getPurchaseId()).
                    orElseThrow(() -> new ObjectNotFoundException("messages.purchase.notFound", request.getPurchaseId()));
            existingContract.setProductPurchase(purchase);
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
        if (request.getMonths() != null) {
            existingContract.setMonths(request.getMonths());
        }
        // Check if the down payment is less than the final price

        if( request.getFinalPrice() != null ) {
            existingContract.setFinalPrice(request.getFinalPrice());
        }
        if (request.getDownPayment() != null ) {
            existingContract.setDownPayment(request.getDownPayment());
        }
        if (request.getAdditionalCosts() != null) {
            existingContract.setAdditionalCosts(request.getAdditionalCosts());
        }

        // Auto calculate original price = purchase price + additional costs
        BigDecimal originalPrice = existingContract.getProductPurchase().getBuyPrice().add(existingContract.getAdditionalCosts());
        existingContract.setOriginalPrice(originalPrice);
        if (request.getAgreedPaymentDay() != null) {
            existingContract.setAgreedPaymentDay(request.getAgreedPaymentDay());
        }
        // Validate financials
        validateFinancials(request.getFinalPrice(),
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


        // Auto calculate the remaining amount and monthly amount
        BigDecimal remaining = existingContract.getFinalPrice()
                .subtract(existingContract.getDownPayment());
        existingContract.setRemainingAmount(remaining);

        existingContract.setMonthlyAmount(
                remaining.divide(
                        BigDecimal.valueOf(existingContract.getMonths()),
                        2,
                        RoundingMode.HALF_UP
                )
        );

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
    public Page<ContractResponse> getCustomerWithContracts(Long id, int page, int size) {
        boolean customerExists = customerRepository.existsByIdAndActiveTrue(id);
        if (!customerExists) {
            throw new UserNotFoundException("messages.customer.notFound", id);
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return contractRepository.findByCustomerId(id, pageable);
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
     * Delete a contract by ID
     * Soft delete could be implemented if needed
     *
     */


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
    private void applyDefaults(Contract contract) {
        if (contract.getAdditionalCosts() == null)
            contract.setAdditionalCosts(BigDecimal.ZERO);

        if (contract.getAgreedPaymentDay() == null)
            contract.setAgreedPaymentDay(1);

        if (contract.getCashDiscountRate() == null)
            contract.setCashDiscountRate(BigDecimal.ZERO);

        if (contract.getEarlyPaymentDiscountRate() == null)
            contract.setEarlyPaymentDiscountRate(BigDecimal.ZERO);
   // TODO auto calculate final price
        if (contract.getFinalPrice() == null){

        }
    }

    /**
     * Calculate profit amount based on purchase price and final price
     * Requirement #15: Calculate original price + markup + additional costs
     */
    private BigDecimal calculateProfit(Contract contract) {        // Profit = Final Price - Original Price (which includes purchase + additional costs)
        return contract.getFinalPrice().subtract(contract.getOriginalPrice());
    }

}