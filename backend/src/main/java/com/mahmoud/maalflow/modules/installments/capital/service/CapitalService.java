package com.mahmoud.maalflow.modules.installments.capital.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.exception.ObjectNotFoundException;
import com.mahmoud.maalflow.modules.installments.capital.entity.CapitalPool;
import com.mahmoud.maalflow.modules.installments.capital.entity.CapitalTransaction;
import com.mahmoud.maalflow.modules.installments.capital.enums.CapitalTransactionType;
import com.mahmoud.maalflow.modules.installments.capital.repo.CapitalPoolRepository;
import com.mahmoud.maalflow.modules.installments.capital.repo.CapitalTransactionRepository;
import com.mahmoud.maalflow.modules.installments.contract.entity.Contract;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Capital Service - Pooled Capital Model
 *
 * All contracts draw from a single shared capital pool.
 * - Pool tracks available and locked amounts
 * - Transactions logged for audit trail
 * - Partner_id in contract is reference only (not for capital allocation)
 *
 * Requirements addressed:
 * - Req 10: Capital calculation and return
 * - Capital returned from principal portions
 * - Partnership contribution tracking
 *
 * @author Mahmoud
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CapitalService {

    private final CapitalPoolRepository capitalPoolRepository;
    private final CapitalTransactionRepository capitalTransactionRepository;

    private static final Long DEFAULT_POOL_ID = 1L;

    /**
     * Allocate capital from pool when contract is created.
     *
     * Process:
     * 1. Check pool has sufficient available capital
     * 2. Move amount from AVAILABLE to LOCKED
     * 3. Record allocation transaction with before/after balances
     * 4. Update contract with allocated amount
     *
     * @param contract Contract requiring capital
     * @param amount Capital to allocate
     * @param currentUser User performing allocation
     * @throws BusinessException if insufficient capital
     */
    @Transactional
    public void allocateCapitalForContract(Contract contract, BigDecimal amount, User currentUser) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.debug("No capital to allocate: {}", amount);
            return;
        }

        log.info("Allocating {} capital for contract {}", amount, contract.getId());

        CapitalPool pool = getPool();

        // Check available capital
        if (pool.getAvailableAmount().compareTo(amount) < 0) {
            throw new BusinessException(
                String.format("Insufficient available capital. Available: %s, Required: %s",
                    pool.getAvailableAmount(), amount)
            );
        }

        // Record before state
        BigDecimal availableBefore = pool.getAvailableAmount();
        BigDecimal lockedBefore = pool.getLockedAmount();

        // Move from available to locked
        pool.setAvailableAmount(availableBefore.subtract(amount));
        pool.setLockedAmount(lockedBefore.add(amount));
        capitalPoolRepository.save(pool);

        // Update contract
        contract.setCapitalAllocated(amount);

        // Record transaction
        recordTransaction(
            pool,
            CapitalTransactionType.ALLOCATION,
            amount,
            availableBefore,
            pool.getAvailableAmount(),
            lockedBefore,
            pool.getLockedAmount(),
            "CONTRACT",
            contract.getId(),
            contract.getId(),
            contract.getPartner() != null ? contract.getPartner().getId() : null,
            currentUser,
            String.format("Capital allocated for contract %d", contract.getId())
        );

        log.info("Capital allocated: {} from pool. Available: {} → {}, Locked: {} → {}",
            amount,
            availableBefore, pool.getAvailableAmount(),
            lockedBefore, pool.getLockedAmount()
        );
    }

    /**
     * Return capital from payment (principal portion freed).
     *
     * Process:
     * 1. Get pool and validate
     * 2. Move amount from LOCKED back to AVAILABLE (capital is freed)
     * 3. Record return transaction with before/after balances
     * 4. Update contract with returned amount
     *
     * This is called during payment processing when principal is paid.
     * NO partner null check - capital is always returned to pool.
     *
     * @param contract Contract the payment is for
     * @param principalPaid Principal portion of payment (to be returned as capital)
     * @param paymentId Payment that triggered return
     * @param currentUser User processing payment
     */
    @Transactional
    public void returnCapitalFromPayment(Contract contract, BigDecimal principalPaid,
                                        Long paymentId, User currentUser) {

        if (principalPaid == null || principalPaid.compareTo(BigDecimal.ZERO) <= 0) {
            log.debug("No capital to return from payment");
            return;
        }

        log.info("Returning {} capital from payment {} for contract {}",
            principalPaid, paymentId, contract.getId());

        CapitalPool pool = getPool();

        if (pool.getLockedAmount().compareTo(principalPaid) < 0) {
            log.warn("Locked amount {} < return amount {}. Pool may be inconsistent.",
                pool.getLockedAmount(), principalPaid);
        }

        // Record before state
        BigDecimal lockedBefore = pool.getLockedAmount();
        BigDecimal availableBefore = pool.getAvailableAmount();

        // Move from locked back to available (capital is freed)
        pool.setLockedAmount(lockedBefore.subtract(principalPaid));
        pool.setAvailableAmount(availableBefore.add(principalPaid));
        pool.setReturnedAmount(pool.getReturnedAmount().add(principalPaid));
        capitalPoolRepository.save(pool);

        // Update contract
        BigDecimal currentReturned = contract.getCapitalReturned() != null
            ? contract.getCapitalReturned()
            : BigDecimal.ZERO;
        contract.setCapitalReturned(currentReturned.add(principalPaid));

        // Record transaction
        recordTransaction(
            pool,
            CapitalTransactionType.RETURN,
            principalPaid,
            availableBefore,
            pool.getAvailableAmount(),
            lockedBefore,
            pool.getLockedAmount(),
            "PAYMENT",
            paymentId,
            contract.getId(),
            contract.getPartner() != null ? contract.getPartner().getId() : null,
            currentUser,
            String.format("Capital returned from payment %d (principal: %s)", paymentId, principalPaid)
        );

        log.info("Capital returned: {} to pool. Locked: {} → {}, Available: {} → {}",
            principalPaid,
            lockedBefore, pool.getLockedAmount(),
            availableBefore, pool.getAvailableAmount()
        );
    }

    /**
     * Get pool status (current available/locked amounts)
     */
    @Transactional(readOnly = true)
    public CapitalPool getPoolStatus() {
        return getPool();
    }

    /**
     * Get or create default pool
     */
    private CapitalPool getPool() {
        return capitalPoolRepository.findById(DEFAULT_POOL_ID)
            .orElseThrow(() -> new ObjectNotFoundException(
                "messages.capital.poolNotFound", DEFAULT_POOL_ID)
            );
    }

    /**
     * Record a capital transaction with complete before/after state.
     * This maintains audit trail and allows balance verification.
     *
     * @param pool Capital pool
     * @param type Transaction type
     * @param amount Transaction amount
     * @param availableBefore Available before transaction
     * @param availableAfter Available after transaction
     * @param lockedBefore Locked before transaction
     * @param lockedAfter Locked after transaction
     * @param referenceType Type of entity that triggered transaction
     * @param referenceId ID of entity that triggered transaction
     * @param contractId Related contract (if applicable)
     * @param partnerId Related partner (if applicable)
     * @param user User who triggered transaction
     * @param description Human-readable description
     */
    private void recordTransaction(CapitalPool pool,
                                  CapitalTransactionType type,
                                  BigDecimal amount,
                                  BigDecimal availableBefore,
                                  BigDecimal availableAfter,
                                  BigDecimal lockedBefore,
                                  BigDecimal lockedAfter,
                                  String referenceType,
                                  Long referenceId,
                                  Long contractId,
                                  Long partnerId,
                                  User user,
                                  String description) {

        CapitalTransaction transaction = CapitalTransaction.builder()
            .capitalPool(pool)
            .transactionType(type)
            .amount(amount)
            .availableBefore(availableBefore)
            .availableAfter(availableAfter)
            .lockedBefore(lockedBefore)
            .lockedAfter(lockedAfter)
            .referenceType(referenceType)
            .referenceId(referenceId)
            .contractId(contractId)
            .partnerId(partnerId)
            .description(description)
            .transactionDate(LocalDate.now())
            .createdBy(user)
            .build();

        capitalTransactionRepository.save(transaction);

        log.debug("Capital transaction recorded: type={}, amount={}, description={}",
            type, amount, description);
    }
}

