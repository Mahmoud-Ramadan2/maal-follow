package com.mahmoud.maalflow.modules.installments.ledger.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.exception.ObjectNotFoundException;
import com.mahmoud.maalflow.modules.installments.ledger.dto.*;
import com.mahmoud.maalflow.modules.installments.ledger.entity.DailyLedger;
import com.mahmoud.maalflow.modules.installments.ledger.enums.LedgerReferenceType;
import com.mahmoud.maalflow.modules.installments.ledger.enums.LedgerSource;
import com.mahmoud.maalflow.modules.installments.ledger.enums.LedgerType;
import com.mahmoud.maalflow.modules.installments.ledger.mapper.LedgerMapper;
import com.mahmoud.maalflow.modules.installments.ledger.repo.DailyLedgerRepository;
import com.mahmoud.maalflow.modules.installments.partner.entity.Partner;
import com.mahmoud.maalflow.modules.installments.partner.repo.PartnerRepository;
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
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing daily ledger entries with comprehensive business validation.
 * Implements idempotency to prevent duplicate ledger entries.
 *
 * @author Mahmoud
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LedgerService {

    private final DailyLedgerRepository ledgerRepository;
    private final LedgerMapper ledgerMapper;
    private final UserRepository userRepository;
    private final PartnerRepository partnerRepository;

    /**
     * Creates a new ledger entry with idempotency check.
     * If the same idempotency key is used, returns the existing entry.
     */
    @Transactional
    public LedgerResponse createLedgerEntry(LedgerRequest request) {
        log.info("Creating ledger entry with idempotency key: {}", request.getIdempotencyKey());

        // Idempotency Check: Return existing entry if already processed
        Optional<DailyLedger> existingEntry = ledgerRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existingEntry.isPresent()) {
            log.info("Idempotent request detected. Returning existing ledger entry ID: {}", 
                    existingEntry.get().getId());
            return ledgerMapper.toResponse(existingEntry.get());
        }

        // Business Rule 1: Validate amount is positive
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("messages.ledger.amountMustBePositive");
        }

        // Business Rule 2: Validate date is not in the future
        if (request.getDate().isAfter(LocalDate.now())) {
            throw new BusinessException("messages.ledger.futureDateNotAllowed");
        }

        // Business Rule 3: Validate reference consistency
        if (request.getReferenceType() != null && request.getReferenceId() == null) {
            throw new BusinessException("messages.ledger.referenceIdRequired");
        }

        // Create ledger entry
        DailyLedger ledger = ledgerMapper.toEntity(request);

        // Set user (TODO: get from security context)
        User currentUser = userRepository.findById(1L)
                .orElseThrow(() -> new ObjectNotFoundException("messages.user.notFound", 1L));
        ledger.setUser(currentUser);

        // Set partner if provided
        if (request.getPartnerId() != null) {
            Partner partner = partnerRepository.findById(request.getPartnerId())
                    .orElseThrow(() -> new ObjectNotFoundException(
                            "messages.partner.notFound", request.getPartnerId()));
            ledger.setPartner(partner);
        }

        DailyLedger savedLedger = ledgerRepository.save(ledger);
        log.info("Successfully created ledger entry with ID: {} - Type: {} Amount: {}", 
                savedLedger.getId(), savedLedger.getType(), savedLedger.getAmount());

        return ledgerMapper.toResponse(savedLedger);
    }

    /**
     * Gets ledger entry by ID.
     */
    @Transactional(readOnly = true)
    public LedgerResponse getById(Long id) {
        DailyLedger ledger = ledgerRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("messages.ledger.notFound", id));
        return ledgerMapper.toResponse(ledger);
    }

    /**
     * Gets ledger entry by idempotency key.
     */
    @Transactional(readOnly = true)
    public Optional<LedgerResponse> getByIdempotencyKey(String idempotencyKey) {
        return ledgerRepository.findByIdempotencyKey(idempotencyKey)
                .map(ledgerMapper::toResponse);
    }

    /**
     * Updates an existing ledger entry.
     */
    @Transactional
    public LedgerResponse update(Long id, LedgerRequest request) {
        log.info("Updating ledger entry with ID: {}", id);

        DailyLedger existingLedger = ledgerRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("messages.ledger.notFound", id));


        // Update fields
        ledgerMapper.updateEntityFromRequest(request, existingLedger);

        // Update partner if changed
        if (request.getPartnerId() != null) {
            Partner partner = partnerRepository.findById(request.getPartnerId())
                    .orElseThrow(() -> new ObjectNotFoundException(
                            "messages.partner.notFound", request.getPartnerId()));
            existingLedger.setPartner(partner);
        } else {
            existingLedger.setPartner(null);
        }
        if (request.getAmount() != null && request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("messages.ledger.amountMustBePositive");
        } else {
            existingLedger.setAmount(request.getAmount());
        }

        if(request.getDate() != null && request.getDate().isAfter(LocalDate.now())) {
            throw new BusinessException("messages.ledger.futureDateNotAllowed");
        } else {
            existingLedger.setDate(request.getDate());
        }

        if(request.getReferenceType() != null && request.getReferenceId() == null) {
            throw new BusinessException("messages.ledger.referenceIdRequired");
        }

        DailyLedger updatedLedger = ledgerRepository.save(existingLedger);
        log.info("Successfully updated ledger entry with ID: {}", id);

        return ledgerMapper.toResponse(updatedLedger);
    }

    /**
     * Lists all ledger entries with pagination.
     */
    @Transactional(readOnly = true)
    public Page<LedgerSummary> list(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date", "createdAt"));

        Page<DailyLedger> ledgerPage;
        if (search != null && !search.isBlank()) {
            ledgerPage = ledgerRepository.searchByDescription(search.trim(), pageable);
        } else {
            ledgerPage = ledgerRepository.findAll(pageable);
        }

        return ledgerPage.map(ledgerMapper::toSummary);
    }

    /**
     * Lists ledger entries by date.
     */
    @Transactional(readOnly = true)
    public Page<LedgerSummary> listByDate(LocalDate date, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ledgerRepository.findByDate(date, pageable).map(ledgerMapper::toSummary);
    }

    /**
     * Lists ledger entries by type.
     */
    @Transactional(readOnly = true)
    public Page<LedgerSummary> listByType(LedgerType type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date", "createdAt"));
        return ledgerRepository.findByType(type, pageable).map(ledgerMapper::toSummary);
    }

    /**
     * Lists ledger entries by source.
     */
    @Transactional(readOnly = true)
    public Page<LedgerSummary> listBySource(LedgerSource source, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date", "createdAt"));
        return ledgerRepository.findBySource(source, pageable).map(ledgerMapper::toSummary);
    }

    /**
     * Lists ledger entries within a date range.
     */
    @Transactional(readOnly = true)
    public Page<LedgerSummary> listByDateRange(LocalDate startDate, LocalDate endDate, int page, int size) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("messages.ledger.invalidDateRange");
        }
        Pageable pageable = PageRequest.of(page, size);
        return ledgerRepository.findByDateBetween(startDate, endDate, pageable).map(ledgerMapper::toSummary);
    }

    /**
     * Lists ledger entries by partner.
     */
    @Transactional(readOnly = true)
    public Page<LedgerSummary> listByPartner(Long partnerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date", "createdAt"));
        return ledgerRepository.findByPartnerId(partnerId, pageable).map(ledgerMapper::toSummary);
    }

    /**
     * Gets daily ledger summary for a date range.
     */
    @Transactional(readOnly = true)
    public List<DailyLedgerSummary> getDailySummary(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("messages.ledger.invalidDateRange");
        }

        List<Object[]> results = ledgerRepository.getDailySummary(startDate, endDate);

        return results.stream()
                .map(row -> {
                    BigDecimal income = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
                    BigDecimal expense = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;

                    return DailyLedgerSummary.builder()
                            .date((LocalDate) row[0])
                            .totalIncome(income)
                            .totalExpense(expense)
                            .netAmount(income.subtract(expense))
                            .incomeCount(((Number) row[3]).longValue())
                            .expenseCount(((Number) row[4]).longValue())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Gets ledger statistics.
     */
    @Transactional(readOnly = true)
    public LedgerStatistics getStatistics() {
        LocalDate startOfMonth = YearMonth.now().atDay(1);
        LocalDate endOfMonth = YearMonth.now().atEndOfMonth();
        LocalDate startOfAllTime = LocalDate.of(2000, 1, 1);
        LocalDate today = LocalDate.now();

        BigDecimal totalIncome = ledgerRepository.getTotalIncomeForDateRange(startOfAllTime, today);
        BigDecimal totalExpenses = ledgerRepository.getTotalExpensesForDateRange(startOfAllTime, today);
        BigDecimal incomeThisMonth = ledgerRepository.getTotalIncomeForDateRange(startOfMonth, endOfMonth);
        BigDecimal expensesThisMonth = ledgerRepository.getTotalExpensesForDateRange(startOfMonth, endOfMonth);

        return LedgerStatistics.builder()
                .totalEntries(ledgerRepository.count())
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(totalIncome.subtract(totalExpenses))
                .incomeEntries(ledgerRepository.countByType(LedgerType.INCOME))
                .expenseEntries(ledgerRepository.countByType(LedgerType.EXPENSE))
                .incomeThisMonth(incomeThisMonth)
                .expensesThisMonth(expensesThisMonth)
                .netBalanceThisMonth(incomeThisMonth.subtract(expensesThisMonth))
                .build();
    }

    /**
     * Deletes a ledger entry.
     */
    @Transactional
    public String delete(Long id) {
        log.info("Deleting ledger entry with ID: {}", id);

        DailyLedger ledger = ledgerRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("messages.ledger.notFound", id));

        ledgerRepository.delete(ledger);
        log.info("Successfully deleted ledger entry with ID: {}", id);

        return "messages.ledger.deleted";
    }

    /**
     * Creates an income entry from a payment (called internally when payment is processed).
     */
    @Transactional
    public LedgerResponse createIncomeFromPayment(Long paymentId, BigDecimal amount, String description) {
        String idempotencyKey = "PAYMENT_" + paymentId;
        
        // Check idempotency
        Optional<DailyLedger> existing = ledgerRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return ledgerMapper.toResponse(existing.get());
        }

        LedgerRequest request = LedgerRequest.builder()
                .idempotencyKey(idempotencyKey)
                .type(LedgerType.INCOME)
                .amount(amount)
                .source(LedgerSource.COLLECTION)
                .referenceType(LedgerReferenceType.PAYMENT)
                .referenceId(paymentId)
                .description(description)
                .date(LocalDate.now())
                .build();

        return createLedgerEntry(request);
    }

    /**
     * Creates an expense entry from a purchase (called internally when purchase is recorded).
     */
    @Transactional
    public LedgerResponse createExpenseFromPurchase(Long purchaseId, BigDecimal amount, String description) {
        String idempotencyKey = "PURCHASE_" + purchaseId;
        
        // Check idempotency
        Optional<DailyLedger> existing = ledgerRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return ledgerMapper.toResponse(existing.get());
        }

        LedgerRequest request = LedgerRequest.builder()
                .idempotencyKey(idempotencyKey)
                .type(LedgerType.EXPENSE)
                .amount(amount)
                .source(LedgerSource.PURCHASE)
                .referenceType(LedgerReferenceType.PURCHASE)
                .referenceId(purchaseId)
                .description(description)
                .date(LocalDate.now())
                .build();

        return createLedgerEntry(request);
    }

    /**
     * Creates an expense entry for partner withdrawal (called internally when withdrawal is processed).
     */
    @Transactional
    public LedgerResponse recordPartnerWithdrawalExpense(Long partnerId, BigDecimal amount,
                                                        Long withdrawalId, String description) {
        String idempotencyKey = "WITHDRAWAL_" + withdrawalId;

        // Check idempotency
        Optional<DailyLedger> existing = ledgerRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            log.info("Idempotent withdrawal expense request detected for withdrawal ID: {}", withdrawalId);
            return ledgerMapper.toResponse(existing.get());
        }

        LedgerRequest request = LedgerRequest.builder()
                .idempotencyKey(idempotencyKey)
                .type(LedgerType.EXPENSE)
                .amount(amount)
                .source(LedgerSource.WITHDRAWAL)
                .referenceType(LedgerReferenceType.WITHDRAWAL)
                .referenceId(withdrawalId)
                .partnerId(partnerId)
                .description(description != null ? description : "Partner withdrawal")
                .date(LocalDate.now())
                .build();

        log.info("Recording partner withdrawal expense - Partner ID: {}, Amount: {}, Withdrawal ID: {}",
                 partnerId, amount, withdrawalId);

        return createLedgerEntry(request);
    }

    /**
     * Creates an income entry for partner investment (called internally when investment is confirmed).
     */
    @Transactional
    public LedgerResponse recordPartnerInvestmentIncome(Long partnerId, BigDecimal amount,
                                                       Long investmentId, String description) {
        String idempotencyKey = "INVESTMENT_" + investmentId;

        // Check idempotency
        Optional<DailyLedger> existing = ledgerRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            log.info("Idempotent investment income request detected for investment ID: {}", investmentId);
            return ledgerMapper.toResponse(existing.get());
        }

        LedgerRequest request = LedgerRequest.builder()
                .idempotencyKey(idempotencyKey)
                .type(LedgerType.INCOME)
                .amount(amount)
                .source(LedgerSource.INVESTMENT)
                .referenceType(LedgerReferenceType.INVESTMENT)
                .referenceId(investmentId)
                .partnerId(partnerId)
                .description(description != null ? description : "Partner investment")
                .date(LocalDate.now())
                .build();

        log.info("Recording partner investment income - Partner ID: {}, Amount: {}, Investment ID: {}",
                 partnerId, amount, investmentId);

        return createLedgerEntry(request);
    }
}

