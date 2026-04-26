package com.mahmoud.maalflow.modules.installments.capital.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.exception.ObjectNotFoundException;
import com.mahmoud.maalflow.modules.installments.capital.dto.CapitalPoolRequest;
import com.mahmoud.maalflow.modules.installments.capital.dto.CapitalPoolResponse;
import com.mahmoud.maalflow.modules.installments.capital.entity.CapitalPool;
import com.mahmoud.maalflow.modules.installments.capital.enums.CapitalTransactionType;
import com.mahmoud.maalflow.modules.installments.capital.mapper.CapitalPoolMapper;
import com.mahmoud.maalflow.modules.installments.capital.repo.CapitalPoolRepository;
import com.mahmoud.maalflow.modules.installments.capital.repo.CapitalTransactionRepository;
import com.mahmoud.maalflow.modules.installments.partner.service.PartnerShareService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.mahmoud.maalflow.modules.shared.constants.AppConstants.*;

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
    private final PartnerShareService partnerShareService;



    /**
     * Create a new capital pool entry.
     */
    @Transactional
    public CapitalPoolResponse createCapitalPool(CapitalPoolRequest request) {

        log.info("Creating new capital pool");

        validateCapitalPoolAmounts(request);

        // Existence check only; no lock needed during creation flow.
        if (capitalPoolRepository.findById(DEFAULT_POOL_ID).isPresent()) {
            log.warn("Capital pool with ID {} already exists", DEFAULT_POOL_ID);
            throw new BusinessException("messages.capitalPool.alreadyExists");
        }

        // Create new capital pool from request
        CapitalPool newPool = capitalPoolMapper.toEntity(request);
//        newPool.setId(DEFAULT_POOL_ID);
        if (newPool.getId() == null) {
            newPool.setId(DEFAULT_POOL_ID);
        } else if (!newPool.getId().equals(DEFAULT_POOL_ID)) {
            throw new BusinessException("messages.capitalPool.invalidId");
        }
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
        partnerShareService.recalculateSharePercentages(savedPool.getTotalAmount());

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

        // Lock row for write to avoid concurrent lost updates.
        CapitalPool currentPool = getPoolOrThrowForUpdate();

        validateManualUpdateAllowed();

        BigDecimal requestedTotal = nz(request.getTotalAmount());
        BigDecimal currentLocked = nz(currentPool.getLockedAmount());

        if (requestedTotal.compareTo(currentLocked) < 0) {
            throw new BusinessException("messages.capitalPool.total.lessThanLocked");
        }

        // Derived value: available is always total minus currently locked capital.
        BigDecimal recalculatedAvailable = requestedTotal.subtract(currentLocked);

        // Update pool values
        BigDecimal totalBefore = currentPool.getTotalAmount();
        currentPool.setTotalAmount(requestedTotal);
        currentPool.setOwnerContribution(nz(request.getOwnerContribution()));
        currentPool.setPartnerContributions(nz(request.getPartnerContributions()));
        currentPool.setAvailableAmount(recalculatedAvailable);
        currentPool.setDescription(request.getDescription());

        assertPoolInvariants(currentPool);

        CapitalPool savedPool = capitalPoolRepository.save(currentPool);
        partnerShareService.recalculateSharePercentages(savedPool.getTotalAmount());

        // Log the changes
        logCapitalPoolChanges(totalBefore, savedPool.getTotalAmount());

        return capitalPoolMapper.toResponse(savedPool);
    }

    /**
     * Get current capital pool status.
     */
    @Transactional(readOnly = true)
    public CapitalPoolResponse getCurrentCapitalPool() {
        log.debug("Retrieving current capital pool");

        CapitalPool currentPool = getPoolOrThrow();

        return capitalPoolMapper.toResponse(currentPool);
    }


    /**
     * Recalculate capital pool from transactions.
     */
    @Transactional
    public CapitalPoolResponse recalculateCapitalPool() {
        log.info("Recalculating capital pool from transactions");

        // Lock row for write to keep recalculation atomic under concurrency.
        CapitalPool currentPool = getPoolOrThrowForUpdate();

        BigDecimal totalInvestments = capitalTransactionRepository
                .sumAmountByTransactionType(CapitalTransactionType.INVESTMENT);
        BigDecimal totalWithdrawals = capitalTransactionRepository
                .sumAmountByTransactionType(CapitalTransactionType.WITHDRAWAL);
        BigDecimal totalAllocations = capitalTransactionRepository
                .sumAmountByTransactionType(CapitalTransactionType.ALLOCATION);
        BigDecimal totalReturns = capitalTransactionRepository
                .sumAmountByTransactionType(CapitalTransactionType.RETURN);

        // Rebuild pool state from transaction aggregates while preserving owner contribution.
        BigDecimal ownerContribution = nz(currentPool.getOwnerContribution());
        BigDecimal partnerContributions = nz(totalInvestments).subtract(nz(totalWithdrawals));
        if (partnerContributions.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("messages.capitalPool.partnerContributions.negative");
        }

        BigDecimal lockedAmount = nz(totalAllocations).subtract(nz(totalReturns));
        if (lockedAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("messages.capitalPool.lockedAmount.negative");
        }

        BigDecimal totalAmount = ownerContribution.add(partnerContributions);
        if (totalAmount.compareTo(lockedAmount) < 0) {
            throw new BusinessException("messages.capitalPool.total.lessThanLocked");
        }

        BigDecimal availableAmount = totalAmount.subtract(lockedAmount);
        BigDecimal returnedAmount = nz(totalReturns);

        currentPool.setTotalAmount(totalAmount);
        currentPool.setAvailableAmount(availableAmount);
        currentPool.setLockedAmount(lockedAmount);
        currentPool.setReturnedAmount(returnedAmount);
        currentPool.setOwnerContribution(ownerContribution);
        currentPool.setPartnerContributions(partnerContributions);
        currentPool.setDescription("Recalculated from transactions - " + LocalDate.now());

        assertPoolInvariants(currentPool);

        CapitalPool savedPool = capitalPoolRepository.save(currentPool);
        partnerShareService.recalculateSharePercentages(savedPool.getTotalAmount());

        log.info("Recalculated capital pool - Total: {}, Available: {}, Locked: {}, Returned: {}",
                totalAmount, availableAmount, lockedAmount, returnedAmount);

        return capitalPoolMapper.toResponse(savedPool);
    }

    /**
     * Get capital pool history.
     */
    @Transactional(readOnly = true)
    public Page<CapitalPoolResponse> getCapitalPoolHistory(Pageable pageable) {
        log.debug("Retrieving capital pool history");

        Pageable effectivePageable = normalizeHistoryPageable(pageable);
        Page<CapitalPool> poolHistory = capitalPoolRepository.findAll(effectivePageable);
        return poolHistory.map(capitalPoolMapper::toResponse);
    }

    /**
     * Validate that total amount equals sum of contributions.
     */
    private void validateCapitalPoolAmounts(CapitalPoolRequest request) {
        if (request == null
                || request.getTotalAmount() == null
                || request.getOwnerContribution() == null
                || request.getPartnerContributions() == null) {
            log.error("Capital pool amounts validation failed - request contains null values");
            throw new BusinessException("messages.capitalPool.amounts.mismatch");
        }

        BigDecimal calculatedTotal = request.getOwnerContribution().add(request.getPartnerContributions());

        if (request.getTotalAmount().compareTo(calculatedTotal) != 0) {
            log.error("Capital pool amounts validation failed - Total: {}, Calculated: {}",
                    request.getTotalAmount(), calculatedTotal);
            throw new BusinessException("messages.capitalPool.amounts.mismatch");
        }

        if (request.getTotalAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("messages.capitalPool.totalAmount.positive");
        }
        if (request.getOwnerContribution().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("messages.capitalPool.ownerContribution.positive");
        }
        if (request.getPartnerContributions().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("messages.capitalPool.partnerContributions.positive");
        }
    }

    private void validateManualUpdateAllowed() {
        if (capitalTransactionRepository.count() > 0) {
            throw new BusinessException("messages.capitalPool.manualUpdate.notAllowed");
        }
    }

    private void assertPoolInvariants(CapitalPool pool) {
        BigDecimal total = nz(pool.getTotalAmount());
        BigDecimal available = nz(pool.getAvailableAmount());
        BigDecimal locked = nz(pool.getLockedAmount());
        BigDecimal owner = nz(pool.getOwnerContribution());
        BigDecimal partner = nz(pool.getPartnerContributions());

        if (available.compareTo(BigDecimal.ZERO) < 0 || locked.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("messages.capitalPool.amounts.mismatch");
        }
        if (total.compareTo(owner.add(partner)) != 0) {
            throw new BusinessException("messages.capitalPool.amounts.mismatch");
        }
        if (total.compareTo(available.add(locked)) != 0) {
            throw new BusinessException("messages.capitalPool.amounts.mismatch");
        }
    }

    private BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private Pageable normalizeHistoryPageable(Pageable pageable) {
        Sort defaultSort = Sort.by(Sort.Order.desc("updatedAt"), Sort.Order.desc("createdAt"));

        if (pageable == null || pageable.isUnpaged()) {
            return PageRequest.of(0, DEFAULT_HISTORY_PAGE_SIZE, defaultSort);
        }

        int normalizedPage = Math.max(pageable.getPageNumber(), 0);
        int normalizedSize = pageable.getPageSize() <= 0
                ? DEFAULT_HISTORY_PAGE_SIZE
                : Math.min(pageable.getPageSize(), MAX_HISTORY_PAGE_SIZE);
        Sort effectiveSort = pageable.getSort().isSorted() ? pageable.getSort() : defaultSort;

        return PageRequest.of(normalizedPage, normalizedSize, effectiveSort);
    }

    /**
     * Log capital pool changes for auditing.
     */
    private void logCapitalPoolChanges(BigDecimal oldTotal, BigDecimal newTotal) {
        log.info("Capital pool updated - Old total: {}, New total: {}", oldTotal, newTotal);
        // TODO save on DB
    }

    private CapitalPool getPoolOrThrow() {
        return capitalPoolRepository.findById(DEFAULT_POOL_ID)
                .orElseThrow(() -> new BusinessException("messages.capitalPool.configurationNotFound"));
    }

    /**
     *  Get capital pool with
     *  pessimistic lock for update operations.
     */
    @Transactional
    public CapitalPool getPoolOrThrowForUpdate() {

        return capitalPoolRepository.findByIdForUpdate(DEFAULT_POOL_ID)
                .orElseThrow(() -> new ObjectNotFoundException("messages.capitalPool.configurationNotFound", DEFAULT_POOL_ID));

    }
}
