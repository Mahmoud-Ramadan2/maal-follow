package com.mahmoud.maalflow.modules.installments.capital.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.modules.installments.capital.dto.CapitalTransactionRequest;
import com.mahmoud.maalflow.modules.installments.capital.dto.CapitalTransactionResponse;
import com.mahmoud.maalflow.modules.installments.capital.entity.CapitalPool;
import com.mahmoud.maalflow.modules.installments.capital.entity.CapitalTransaction;
import com.mahmoud.maalflow.modules.installments.capital.enums.CapitalTransactionType;
import com.mahmoud.maalflow.modules.installments.capital.mapper.CapitalTransactionMapper;
import com.mahmoud.maalflow.modules.installments.capital.repo.CapitalPoolRepository;
import com.mahmoud.maalflow.modules.installments.capital.repo.CapitalTransactionRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing capital transactions in pooled capital model.
 * Handles all capital movement tracking and reporting from the shared pool.
 *
 * @author Mahmoud
 */
@Service
@AllArgsConstructor
@Slf4j
public class CapitalTransactionService {

    private final CapitalTransactionRepository capitalTransactionRepository;
    private final CapitalPoolRepository capitalPoolRepository;
    private final CapitalTransactionMapper capitalTransactionMapper;

    private static final Long DEFAULT_POOL_ID = 1L;

    /**
     * Create a manual capital transaction (investment/withdrawal).
     * For pooled capital model - transactions affect the shared pool.
     */
    @Transactional
    public CapitalTransactionResponse createCapitalTransaction(CapitalTransactionRequest request) {
        log.info("Creating capital transaction: type={}, amount={}",
                request.getTransactionType(), request.getAmount());

        validateCapitalTransactionRequest(request);

        // Get the pool
        CapitalPool pool = getPool();

        // Record before state
        BigDecimal availableBefore = pool.getAvailableAmount();
        BigDecimal lockedBefore = pool.getLockedAmount();

        // Apply transaction to pool
        applyTransactionToPool(pool, request);

        // Save pool changes
        capitalPoolRepository.save(pool);

        // Create transaction record
        CapitalTransaction transaction = CapitalTransaction.builder()
                .capitalPool(pool)
                .transactionType(request.getTransactionType())
                .amount(request.getAmount())
                .availableBefore(availableBefore)
                .availableAfter(pool.getAvailableAmount())
                .lockedBefore(lockedBefore)
                .lockedAfter(pool.getLockedAmount())
                .referenceType("MANUAL")
                .description(request.getDescription())
                .transactionDate(LocalDate.now())
                .build();

        CapitalTransaction savedTransaction = capitalTransactionRepository.save(transaction);

        log.info("Successfully created capital transaction with ID: {}", savedTransaction.getId());
        return capitalTransactionMapper.toResponse(savedTransaction);
    }

    /**
     * Get all capital transactions with pagination.
     */
    public Page<CapitalTransactionResponse> getAllCapitalTransactions(Pageable pageable) {
        log.debug("Retrieving capital transactions with pagination");

        Page<CapitalTransaction> transactions = capitalTransactionRepository.findAll(
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "transactionDate")));

        return transactions.map(capitalTransactionMapper::toResponse);
    }

    /**
     * Get capital transactions by type.
     */
    public List<CapitalTransactionResponse> getCapitalTransactionsByType(CapitalTransactionType transactionType) {
        log.debug("Retrieving capital transactions by type: {}", transactionType);

        List<CapitalTransaction> transactions = capitalTransactionRepository
                .findByTransactionTypeOrderByTransactionDateDesc(transactionType);

        return transactions.stream()
                .map(capitalTransactionMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get capital transactions for a specific partner (by partnerId reference).
     */
    public List<CapitalTransactionResponse> getPartnerCapitalTransactions(Long partnerId) {
        log.debug("Retrieving capital transactions for partner: {}", partnerId);

        List<CapitalTransaction> transactions = capitalTransactionRepository
                .findByPartnerIdOrderByTransactionDateDesc(partnerId);

        return transactions.stream()
                .map(capitalTransactionMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get capital transactions within date range.
     */
    public List<CapitalTransactionResponse> getCapitalTransactionsByDateRange(
            LocalDate startDate, LocalDate endDate) {
        log.debug("Retrieving capital transactions between {} and {}", startDate, endDate);

        List<CapitalTransaction> transactions = capitalTransactionRepository
                .findByTransactionDateBetweenOrderByTransactionDateDesc(startDate, endDate);

        return transactions.stream()
                .map(capitalTransactionMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get monthly capital transaction summary.
     */
    public Map<String, BigDecimal> getMonthlySummary(int year, int month) {
        log.debug("Retrieving monthly summary for {}-{}", year, month);

        List<Object[]> summaryData = capitalTransactionRepository.getMonthlySummary(year, month);

        Map<String, BigDecimal> summary = new HashMap<>();
        for (Object[] row : summaryData) {
            CapitalTransactionType type = (CapitalTransactionType) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            summary.put(type.name(), amount);
        }

        // Calculate net capital flow
        BigDecimal inflows = summary.getOrDefault("INVESTMENT", BigDecimal.ZERO)
                .add(summary.getOrDefault("RETURN", BigDecimal.ZERO));
        BigDecimal outflows = summary.getOrDefault("WITHDRAWAL", BigDecimal.ZERO)
                .add(summary.getOrDefault("ALLOCATION", BigDecimal.ZERO));

        summary.put("NET_CAPITAL_FLOW", inflows.subtract(outflows));
        summary.put("TOTAL_INFLOWS", inflows);
        summary.put("TOTAL_OUTFLOWS", outflows);

        return summary;
    }

    /**
     * Get partner capital summary for a date range.
     * Works by filtering transactions by partnerId.
     */
    public Map<String, BigDecimal> getPartnerCapitalSummary(Long partnerId,
                                                           LocalDate startDate,
                                                           LocalDate endDate) {
        log.debug("Retrieving partner capital summary for partner: {} between {} and {}",
                partnerId, startDate, endDate);

        List<CapitalTransaction> transactions = capitalTransactionRepository
                .findByPartnerIdAndTransactionDateBetweenOrderByTransactionDateDesc(
                        partnerId, startDate, endDate);

        Map<String, BigDecimal> summary = new HashMap<>();

        BigDecimal investments = transactions.stream()
                .filter(t -> t.getTransactionType() == CapitalTransactionType.INVESTMENT)
                .map(CapitalTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal withdrawals = transactions.stream()
                .filter(t -> t.getTransactionType() == CapitalTransactionType.WITHDRAWAL)
                .map(CapitalTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal allocations = transactions.stream()
                .filter(t -> t.getTransactionType() == CapitalTransactionType.ALLOCATION)
                .map(CapitalTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal returns = transactions.stream()
                .filter(t -> t.getTransactionType() == CapitalTransactionType.RETURN)
                .map(CapitalTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        summary.put("TOTAL_INVESTMENTS", investments);
        summary.put("TOTAL_WITHDRAWALS", withdrawals);
        summary.put("TOTAL_ALLOCATIONS", allocations);
        summary.put("TOTAL_RETURNS", returns);
        summary.put("NET_PARTNER_CONTRIBUTION", investments.subtract(withdrawals));

        return summary;
    }

    /**
     * Get pool status.
     */
    @Transactional(readOnly = true)
    public CapitalPool getPoolStatus() {
        return getPool();
    }

    /**
     * Apply transaction to pool based on transaction type.
     */
    private void applyTransactionToPool(CapitalPool pool, CapitalTransactionRequest request) {
        BigDecimal amount = request.getAmount();

        switch (request.getTransactionType()) {
            case INVESTMENT:
                pool.setTotalAmount(pool.getTotalAmount().add(amount));
                pool.setAvailableAmount(pool.getAvailableAmount().add(amount));
                pool.setPartnerContributions(pool.getPartnerContributions().add(amount));
                break;

            case WITHDRAWAL:
                if (pool.getAvailableAmount().compareTo(amount) < 0) {
                    throw new BusinessException("validation.capital.insufficientAvailable");
                }
                pool.setTotalAmount(pool.getTotalAmount().subtract(amount));
                pool.setAvailableAmount(pool.getAvailableAmount().subtract(amount));
                pool.setPartnerContributions(pool.getPartnerContributions().subtract(amount));
                break;

            default:
                throw new BusinessException("Unsupported manual transaction type: " + request.getTransactionType());
        }
    }

    /**
     * Get or create default pool.
     */
    private CapitalPool getPool() {
        return capitalPoolRepository.findById(DEFAULT_POOL_ID)
                .orElseThrow(() -> new BusinessException("messages.capital.poolNotFound"));
    }

    /**
     * Validate capital transaction request.
     */
    private void validateCapitalTransactionRequest(CapitalTransactionRequest request) {
        // Validate amount is positive
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("validation.capital.amount.positive");
        }

        // Validate transaction type is manual type only
        if (request.getTransactionType() != CapitalTransactionType.INVESTMENT &&
            request.getTransactionType() != CapitalTransactionType.WITHDRAWAL) {
            throw new BusinessException("Only INVESTMENT and WITHDRAWAL transactions can be created manually");
        }
    }
}
