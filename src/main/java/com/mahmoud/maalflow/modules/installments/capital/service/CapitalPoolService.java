package com.mahmoud.maalflow.modules.installments.capital.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.modules.installments.capital.dto.CapitalPoolRequest;
import com.mahmoud.maalflow.modules.installments.capital.dto.CapitalPoolResponse;
import com.mahmoud.maalflow.modules.installments.capital.entity.CapitalPool;
import com.mahmoud.maalflow.modules.installments.capital.enums.CapitalTransactionType;
import com.mahmoud.maalflow.modules.installments.capital.mapper.CapitalPoolMapper;
import com.mahmoud.maalflow.modules.installments.capital.repo.CapitalPoolRepository;
import com.mahmoud.maalflow.modules.installments.capital.repo.CapitalTransactionRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Service for managing capital pool operations.
 * Handles capital pool tracking and updates for pooled capital model.
 *
 * @author Mahmoud
 */
@Service
@AllArgsConstructor
@Slf4j
public class CapitalPoolService {

    private final CapitalPoolRepository capitalPoolRepository;
    private final CapitalTransactionRepository capitalTransactionRepository;
    private final CapitalPoolMapper capitalPoolMapper;

    private static final Long DEFAULT_POOL_ID = 1L;


    /**
     * Create a new capital pool entry.
     */
    @Transactional
    public CapitalPoolResponse createCapitalPool(CapitalPoolRequest request) {

        log.info("Creating new capital pool");

        validateCapitalPoolAmounts(request);

        // Check if pool already exists
        if (capitalPoolRepository.findById(DEFAULT_POOL_ID).isPresent()) {
            log.warn("Capital pool with ID {} already exists", DEFAULT_POOL_ID);
            throw new BusinessException("validation.capitalPool.alreadyExists");
        }

        // Create new capital pool from request
        CapitalPool newPool = capitalPoolMapper.toEntity(request);
        newPool.setId(DEFAULT_POOL_ID);
        
        // Set initial amounts based on the request
        newPool.setTotalAmount(request.getTotalAmount());
        newPool.setOwnerContribution(request.getOwnerContribution());
        newPool.setPartnerContributions(request.getPartnerContributions());
        newPool.setDescription(request.getDescription());
        
        // Calculate derived amounts
        BigDecimal availableAmount = request.getTotalAmount(); // Initially all amount is available
        newPool.setAvailableAmount(availableAmount);
        newPool.setLockedAmount(BigDecimal.ZERO);
        newPool.setReturnedAmount(BigDecimal.ZERO);

        CapitalPool savedPool = capitalPoolRepository.save(newPool);

        log.info("Created new capital pool with ID: {}, Total amount: {}", savedPool.getId(), savedPool.getTotalAmount());

        return capitalPoolMapper.toResponse(savedPool);
    }
    /**
     * Update capital pool with new amounts.
     */
    @Transactional
    public CapitalPoolResponse updateCapitalPool(CapitalPoolRequest request) {
        log.info("Updating capital pool");

        validateCapitalPoolAmounts(request);

        // Get current pool
        CapitalPool currentPool = capitalPoolRepository.findById(DEFAULT_POOL_ID)
                .orElseThrow(() -> new BusinessException("validation.capitalPool.notFound"));

        // Update pool values
        BigDecimal totalBefore = currentPool.getTotalAmount();
        currentPool.setTotalAmount(request.getTotalAmount());
        currentPool.setOwnerContribution(request.getOwnerContribution());
        currentPool.setPartnerContributions(request.getPartnerContributions());
        currentPool.setDescription(request.getDescription());

        CapitalPool savedPool = capitalPoolRepository.save(currentPool);

        // Log the changes
        logCapitalPoolChanges(totalBefore, savedPool.getTotalAmount());

        return capitalPoolMapper.toResponse(savedPool);
    }

    /**
     * Get current capital pool status.
     */
    public CapitalPoolResponse getCurrentCapitalPool() {
        log.debug("Retrieving current capital pool");

        CapitalPool currentPool = capitalPoolRepository.findById(DEFAULT_POOL_ID)
                .orElseThrow(() -> new BusinessException("validation.capitalPool.notFound"));

        return capitalPoolMapper.toResponse(currentPool);
    }


    /**
     * Recalculate capital pool from transactions.
     */
    @Transactional
    public CapitalPoolResponse recalculateCapitalPool() {
        log.info("Recalculating capital pool from transactions");

        BigDecimal totalInvestments = capitalTransactionRepository
                .sumAmountByTransactionType(CapitalTransactionType.INVESTMENT);
        BigDecimal totalWithdrawals = capitalTransactionRepository
                .sumAmountByTransactionType(CapitalTransactionType.WITHDRAWAL);
        BigDecimal totalAllocations = capitalTransactionRepository
                .sumAmountByTransactionType(CapitalTransactionType.ALLOCATION);
        BigDecimal totalReturns = capitalTransactionRepository
                .sumAmountByTransactionType(CapitalTransactionType.RETURN);

        // Calculate current amounts
        BigDecimal partnerContributions = (totalInvestments != null ? totalInvestments : BigDecimal.ZERO)
                .subtract(totalWithdrawals != null ? totalWithdrawals : BigDecimal.ZERO);
        BigDecimal lockedAmount = (totalAllocations != null ? totalAllocations : BigDecimal.ZERO)
                .subtract(totalReturns != null ? totalReturns : BigDecimal.ZERO);
        BigDecimal totalAmount = partnerContributions;
        BigDecimal availableAmount = totalAmount.subtract(lockedAmount);
        BigDecimal returnedAmount = totalReturns != null ? totalReturns : BigDecimal.ZERO;

        // Get current pool and update
        CapitalPool currentPool = capitalPoolRepository.findById(DEFAULT_POOL_ID)
                .orElseThrow(() -> new BusinessException("validation.capitalPool.notFound"));

        currentPool.setTotalAmount(totalAmount);
        currentPool.setAvailableAmount(availableAmount);
        currentPool.setLockedAmount(lockedAmount);
        currentPool.setReturnedAmount(returnedAmount);
        currentPool.setOwnerContribution(BigDecimal.ZERO);
        currentPool.setPartnerContributions(partnerContributions);
        currentPool.setDescription("Recalculated from transactions - " + LocalDate.now());

        CapitalPool savedPool = capitalPoolRepository.save(currentPool);

        log.info("Recalculated capital pool - Total: {}, Available: {}, Locked: {}, Returned: {}",
                totalAmount, availableAmount, lockedAmount, returnedAmount);

        return capitalPoolMapper.toResponse(savedPool);
    }

    /**
     * Get capital pool history.
     */
    public Page<CapitalPoolResponse> getCapitalPoolHistory(Pageable pageable) {
        log.debug("Retrieving capital pool history");

        Page<CapitalPool> poolHistory = capitalPoolRepository.findAll(pageable);
        return poolHistory.map(capitalPoolMapper::toResponse);
    }

    /**
     * Validate that total amount equals sum of contributions.
     */
    private void validateCapitalPoolAmounts(CapitalPoolRequest request) {
        BigDecimal calculatedTotal = request.getOwnerContribution().add(request.getPartnerContributions());

        if (request.getTotalAmount().compareTo(calculatedTotal) != 0) {
            log.error("Capital pool amounts validation failed - Total: {}, Calculated: {}",
                    request.getTotalAmount(), calculatedTotal);
            throw new BusinessException("validation.capitalPool.amounts.mismatch");
        }
    }

    /**
     * Log capital pool changes for auditing.
     */
    private void logCapitalPoolChanges(BigDecimal oldTotal, BigDecimal newTotal) {
        log.info("Capital pool updated - Old total: {}, New total: {}", oldTotal, newTotal);
    }


}
