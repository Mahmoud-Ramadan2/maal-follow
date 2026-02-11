package com.mahmoud.nagieb.modules.installments.payment.service;

import com.mahmoud.nagieb.exception.BusinessException;
import com.mahmoud.nagieb.exception.ObjectNotFoundException;
import com.mahmoud.nagieb.modules.installments.capital.service.CapitalService;
import com.mahmoud.nagieb.modules.installments.contract.dto.ContractExpenseRequest;
import com.mahmoud.nagieb.modules.installments.contract.entity.Contract;
import com.mahmoud.nagieb.modules.installments.contract.entity.InstallmentSchedule;
import com.mahmoud.nagieb.modules.installments.contract.enums.ContractStatus;
import com.mahmoud.nagieb.modules.installments.contract.repo.ContractRepository;
import com.mahmoud.nagieb.modules.installments.contract.repo.InstallmentScheduleRepository;
import com.mahmoud.nagieb.modules.installments.contract.service.ContractExpenseService;
import com.mahmoud.nagieb.modules.installments.contract.service.InstallmentScheduleService;
import com.mahmoud.nagieb.modules.installments.ledger.dto.LedgerRequest;
import com.mahmoud.nagieb.modules.installments.ledger.enums.LedgerReferenceType;
import com.mahmoud.nagieb.modules.installments.ledger.enums.LedgerSource;
import com.mahmoud.nagieb.modules.installments.ledger.enums.LedgerType;
import com.mahmoud.nagieb.modules.installments.ledger.service.LedgerService;
import com.mahmoud.nagieb.modules.installments.payment.dto.*;
import com.mahmoud.nagieb.modules.installments.payment.entity.Payment;
import com.mahmoud.nagieb.modules.installments.payment.mapper.PaymentMapper;
import com.mahmoud.nagieb.modules.installments.payment.repo.PaymentRepository;
import com.mahmoud.nagieb.modules.installments.profit.service.ProfitProcessingService;
import com.mahmoud.nagieb.modules.installments.contract.enums.ExpenseType;
import com.mahmoud.nagieb.modules.shared.user.entity.User;
import com.mahmoud.nagieb.modules.shared.user.repo.UserRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static com.mahmoud.nagieb.modules.installments.payment.service.PaymentStatisticsService.getDailyPaymentSummaries;

/**
 * Service for managing payments with comprehensive business validation.
 * Implements idempotency to prevent duplicate payment processing.
 *
 * @author Mahmoud
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final UserRepository userRepository;
    private final InstallmentScheduleRepository installmentScheduleRepository;
    private final ContractRepository contractRepository;
    private final PaymentDiscountService discountService;
    private final LedgerService ledgerService;
    private final ContractExpenseService contractExpenseService;
    private final PaymentProcessingService paymentProcessingService;

    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    /**
     *  PaymentService is responsible for processing payments
     * Implements requirements 7, 17: 
     * discount calculation and reminder management.
     */
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        
        log.info("Processing payment with idempotency key: {}", request.getIdempotencyKey());

        //   1. IDEMPOTENCY CHECK  
        Optional<Payment> existingPayment = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existingPayment.isPresent()) {
            log.info("Idempotent request detected. Returning existing payment ID: {}",
                    existingPayment.get().getId());
            return paymentMapper.toPaymentResponse(existingPayment.get());
        }

        //   2. VALIDATION  
        validatePaymentRequest(request);

        // Get current user (TODO: get from security context)
        User currentUser = userRepository.findById(1L)
                .orElseThrow(() -> new ObjectNotFoundException("messages.user.notFound", 1L));

        // Get installment schedule if provided
        InstallmentSchedule schedule = null;
        Contract contract = null;
        if (request.getInstallmentScheduleId() != null) {
            schedule = installmentScheduleRepository.findById(request.getInstallmentScheduleId())
                    .orElseThrow(() -> new ObjectNotFoundException(
                            "messages.installmentSchedule.notFound", request.getInstallmentScheduleId()));
            contract = schedule.getContract();

            // Validate schedule can be paid
            if (schedule.getStatus() == com.mahmoud.nagieb.modules.installments.contract.enums.PaymentStatus.CANCELLED) {
                throw new BusinessException("messages.schedule.canceledCannotPay");
            }
            if (schedule.getStatus() == com.mahmoud.nagieb.modules.installments.contract.enums.PaymentStatus.PAID) {
                throw new BusinessException("messages.schedule.alreadyPaid");
            }
        }

        //   3. CALCULATE DISCOUNT
        //   manual discount if provided or max(final, early )
        BigDecimal finalDiscount = calculateFinalDiscount(request, schedule);

        //   4. CREATE PAYMENT RECORD
        Payment payment = createPaymentEntity(request, schedule, finalDiscount);
        if (payment.getNotes() == null && schedule != null) {
            payment.setNotes("دفعة مرتبطة بالقسط رقم " + schedule.getSequenceNumber() +
                    " - عقد رقم " + schedule.getContract().getId());
        }
        Payment savedPayment = paymentRepository.save(payment);
        log.info("Created payment record with ID: {}", savedPayment.getId());

        //   5. RECORD IN DAILY LEDGER
        recordPaymentInLedger(savedPayment, schedule, currentUser);

        //   6. RECORD EXPENSE IF PROVIDED
        if (request.getExtraExpenses() != null && request.getExtraExpenses().compareTo(BigDecimal.ZERO) > 0) {
            recordExpense(savedPayment, request.getExtraExpenses(), schedule, contract);
        }

        //   7. PROCESS INSTALLMENT PAYMENT (if linked)  
        if (schedule != null) {
//            paymentProcessingService.processInstallmentPayment(schedule, contract, savedPayment, finalDiscount, currentUser);

            processInstallmentPayment(schedule, contract, savedPayment, finalDiscount, currentUser);
        }

        log.info("Successfully processed payment with ID: {} for amount: {}",
                savedPayment.getId(), savedPayment.getNetAmount());

        return paymentMapper.toPaymentResponse(savedPayment);
    }

    /**
     * Process installment-specific payment logic:
     * - Update schedule status
     * - Return capital (principal portion)
     * - Process profit
     * - Update contract progress
     * - Handle overpayment
     **/
    public void processInstallmentPayment(InstallmentSchedule schedule, Contract contract,
                                          Payment payment, BigDecimal finalDiscount, User currentUser) {

        // Delegate core processing to PaymentProcessingService
        BigDecimal overpayment = paymentProcessingService.processInstallmentPayment(
                schedule, contract, payment, finalDiscount, currentUser);

        // Handle overpayment by applying to next unpaid schedules
        if (overpayment.compareTo(BigDecimal.ZERO) > 0) {
            applyOverpaymentToNextSchedules(contract.getId(), overpayment, payment, currentUser);
        }
    }

    /**
     * Apply overpayment to next unpaid schedule(s)
     */
    private void applyOverpaymentToNextSchedules(Long contractId, BigDecimal overpayment,
                                                  Payment originalPayment, User user) {

        if (overpayment == null || overpayment.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        List<InstallmentSchedule> schedules = installmentScheduleRepository
                .findByContractIdOrderBySequenceNumberAsc(contractId);

        List<InstallmentSchedule> unpaidOrPartialSchedules = schedules.stream()
                .filter(s -> s.getStatus() != com.mahmoud.nagieb.modules.installments.contract.enums.PaymentStatus.CANCELLED
                        && s.getStatus() != com.mahmoud.nagieb.modules.installments.contract.enums.PaymentStatus.PAID)
                .toList();

        if (unpaidOrPartialSchedules.isEmpty()) {
            log.info("No unpaid schedules remaining for overpayment of {} on contract {}", overpayment, contractId);
            return;
        }

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ObjectNotFoundException("messages.contract.notFound", contractId));
        BigDecimal remainingOverpayment = overpayment;

        for (InstallmentSchedule nextUnpaid : unpaidOrPartialSchedules) {
            if (remainingOverpayment.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal currentPaid = nextUnpaid.getPaidAmount() != null
                    ? nextUnpaid.getPaidAmount() : BigDecimal.ZERO;
            BigDecimal totalDiscount = nextUnpaid.getDiscountApplied() != null
                    ? nextUnpaid.getDiscountApplied() : BigDecimal.ZERO;
            BigDecimal amountDue = nextUnpaid.getAmount().subtract(totalDiscount).subtract(currentPaid);

            if (amountDue.compareTo(BigDecimal.ZERO) <= 0) {
                // This schedule is already fully paid
                continue;
            }

            // Calculate how much to apply to this schedule
            BigDecimal amountToApply = remainingOverpayment.min(amountDue);

            // Create a modified payment for this overpayment application
            Payment overpaymentPayment = Payment.builder()
                    .idempotencyKey(originalPayment.getIdempotencyKey() + "-OVER-" + nextUnpaid.getId())
                    .installmentSchedule(nextUnpaid)
                    .amount(amountToApply)
                    .netAmount(amountToApply)
                    .agreedPaymentMonth(nextUnpaid.getProfitMonth())
                    .paymentMethod(originalPayment.getPaymentMethod())
                    .status(originalPayment.getStatus())
                    .paymentDate(originalPayment.getPaymentDate())
                    .actualPaymentDate(originalPayment.getActualPaymentDate())
                    .isEarlyPayment(originalPayment.getActualPaymentDate().isBefore(nextUnpaid.getDueDate()))
                    .discountAmount(BigDecimal.ZERO)
                    .receivedBy(user)
                    .notes("دفعة زائدة من القسط السابق - مطبقة على القسط رقم " + nextUnpaid.getSequenceNumber())
                    .build();

            remainingOverpayment = remainingOverpayment.subtract(amountToApply);

            // Process this schedule payment using PaymentProcessingService (no further overpayment handling needed here)
            paymentProcessingService.processInstallmentPayment(nextUnpaid, contract, overpaymentPayment, BigDecimal.ZERO, user);

            log.info("Applied overpayment of {} to schedule {} (contract {})",
                    amountToApply, nextUnpaid.getId(), contractId);
        }

        if (remainingOverpayment.compareTo(BigDecimal.ZERO) > 0) {
            log.warn("All schedules paid. Remaining Amount of {} on contract {}", remainingOverpayment, contractId);
            // TODO: Could record as credit on customer account or refund
            //refundPayment()
        }
    }


    /**
     * Record payment in daily ledger (cash tracking)
     */
    private void recordPaymentInLedger(Payment payment, InstallmentSchedule schedule, User user) {
        String description = schedule != null
                ? "دفعة قسط رقم " + schedule.getSequenceNumber() + " - عقد رقم " + schedule.getContract().getContractNumber()
                : "دفعة عامة";

        LedgerRequest ledgerRequest = LedgerRequest.builder()
                .idempotencyKey("LEDGER-PAY-" + payment.getId())
                .type(LedgerType.INCOME)
                .amount(payment.getNetAmount())
                .source(schedule!= null? LedgerSource.COLLECTION: LedgerSource.MANUAL)
                .referenceType(LedgerReferenceType.PAYMENT)
                .referenceId(payment.getId())
                .description(description)
                .date(payment.getActualPaymentDate())
                .build();

        ledgerService.createLedgerEntry(ledgerRequest);
        log.info("Recorded payment {} in daily ledger", payment.getId());
    }

    /**
     * Record expense in daily ledger (cash outflow)
     */
    private void recordExpenseInLedger(Payment payment,
                                       BigDecimal expenseAmount,
                                       InstallmentSchedule schedule) {
        String description = schedule != null
                ? "مصاريف قسط رقم " + schedule.getSequenceNumber() + " - عقد رقم " + schedule.getContract().getContractNumber()
                : "مصاريف دفعة رقم " + payment.getId();

        LedgerRequest ledgerRequest = LedgerRequest.builder()
                .idempotencyKey("LEDGER-EXP-" + payment.getId())
                .type(LedgerType.EXPENSE)
                .amount(expenseAmount)
                .source(LedgerSource.OPERATING_EXPENSE)
                .referenceType(LedgerReferenceType.CONTRACT_EXPENSE)
                .referenceId(payment.getId())
                .description(description)
                .date(payment.getActualPaymentDate())
                .build();

        ledgerService.createLedgerEntry(ledgerRequest);
        log.info("Recorded expense {} in daily ledger for payment {}", expenseAmount, payment.getId());
    }


    /**
     * Record expense in ContractExpense table (NOT Deduction!)
     *
     */
    private void recordExpense(Payment payment, BigDecimal expenseAmount,
                                          InstallmentSchedule schedule, Contract contract) {

        String description = schedule != null
                ? "مصاريف إضافية للقسط رقم " + schedule.getSequenceNumber() +
                  " - عقد رقم " + contract.getContractNumber()
                : "مصاريف إضافية - دفعة رقم " + payment.getId();

        ContractExpenseRequest expenseRequest = ContractExpenseRequest.builder()
                .contractId(contract != null ? contract.getId() : null)
                .scheduleId(schedule != null ? schedule.getId() : null)
                .amount(expenseAmount)
                .expenseType(schedule != null ? ExpenseType.INSTALLMENT
                        : ExpenseType.OTHER)
                .description(description)
                .expenseDate(payment.getActualPaymentDate())
                .notes("مرتبط بالدفعة رقم " + payment.getId())
                .build();
        // This will automatically update Ledger contract totals and net profit via DB triggers
       contractExpenseService.createExpense(expenseRequest);
        log.info("Recorded expense of {} for payment {}", expenseAmount, payment.getId());

    }


    /**
     * Gets payment by ID.
     */
    @Transactional(readOnly = true)
    public PaymentResponse getById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("messages.payment.notFound", id));
        return paymentMapper.toPaymentResponse(payment);
    }

    /**
     * Gets payment by idempotency key.
     */
    @Transactional(readOnly = true)
    public Optional<PaymentResponse> getByIdempotencyKey(String idempotencyKey) {
        return paymentRepository.findByIdempotencyKey(idempotencyKey)
                .map(paymentMapper::toPaymentResponse);
    }

    /**
     * Lists all payments with pagination.
     */
    @Transactional(readOnly = true)
    public Page<PaymentSummary> list(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paymentDate"));
        return paymentRepository.findAll(pageable).map(paymentMapper::toPaymentSummary);
    }

    /**
     * Lists payments by agreed payment month.
     */
    @Transactional(readOnly = true)
    public Page<PaymentSummary> listByMonth(String month, int page, int size) {
        validatePaymentMonth(month);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paymentDate"));
        return paymentRepository.findByAgreedPaymentMonth(month, pageable)
                .map(paymentMapper::toPaymentSummary);
    }

    /**
     * Lists payments within a date range.
     */
    @Transactional(readOnly = true)
    public Page<PaymentSummary> listByDateRange(LocalDateTime startDate, LocalDateTime endDate,
                                                int page, int size) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("messages.payment.invalidDateRange");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paymentDate"));
        return paymentRepository.findByPaymentDateBetween(startDate, endDate, pageable)
                .map(paymentMapper::toPaymentSummary);
    }

    /**
     * Lists payments by actual payment date.
     */
    @Transactional(readOnly = true)
    public Page<PaymentSummary> listByActualDate(LocalDate date, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paymentDate"));
        return paymentRepository.findByActualPaymentDate(date, pageable)
                .map(paymentMapper::toPaymentSummary);
    }

    /**
     * Lists early payments.
     */
    @Transactional(readOnly = true)
    public Page<PaymentSummary> listEarlyPayments(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paymentDate"));
        return paymentRepository.findWithFilters(null, null, true, pageable)
                .map(paymentMapper::toPaymentSummary);
    }

    /**
     * Lists payments with discounts.
     */
    @Transactional(readOnly = true)
    public Page<PaymentSummary> listPaymentsWithDiscounts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paymentDate"));
        return paymentRepository.findAll(
                org.springframework.data.jpa.domain.Specification.where(
                        (root, query, criteriaBuilder) ->
                            criteriaBuilder.greaterThan(root.get("discountAmount"), BigDecimal.ZERO)
                ), pageable)
                .map(paymentMapper::toPaymentSummary);
    }

    /**
     * Gets daily payment summary for a date range.
     */
    @Transactional(readOnly = true)
    public List<DailyPaymentSummary> getDailyPaymentSummary(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("messages.payment.invalidDateRange");
        }

        return getDailyPaymentSummaries(startDate, endDate, paymentRepository);
    }

    /**

     * Gets total payments for a specific month.
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalPaymentsForMonth(String month) {
        validatePaymentMonth(month);
        return paymentRepository.sumPaymentsByAgreedMonth(month);
    }

    /**
     * Gets payment statistics.
     */
    @Transactional(readOnly = true)
    public PaymentStatistics getPaymentStatistics() {
        String currentMonth = YearMonth.now().format(MONTH_FORMAT);

        return PaymentStatistics.builder()
                .month(currentMonth)
                .actualPayments(paymentRepository.sumPaymentsByAgreedMonth(currentMonth))
                .totalPaymentCount((int) paymentRepository.count())
                .totalDiscounts(paymentRepository.sumDiscountsForMonth(currentMonth))
                .earlyPaymentCount(paymentRepository.countEarlyPaymentsByMonth(currentMonth))
                .build();
    }

    /**
     * Cancels a payment (soft cancel by changing status).
     */
    @Transactional
    public PaymentResponse cancelPayment(Long id, String reason) {
        log.info("Cancelling payment with ID: {}", id);

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("messages.payment.notFound", id));

        // Business Rule: Cannot cancel already cancelled or refunded payment
        if (payment.getStatus() == com.mahmoud.nagieb.modules.installments.payment.enums.PaymentStatus.CANCELLED ||
                payment.getStatus() == com.mahmoud.nagieb.modules.installments.payment.enums.PaymentStatus.REFUNDED) {
            throw new BusinessException("messages.payment.alreadyCancelledOrRefunded");
        }

        payment.setStatus(com.mahmoud.nagieb.modules.installments.payment.enums.PaymentStatus.CANCELLED);
        if (reason != null && !reason.isBlank()) {
            payment.setNotes((payment.getNotes() != null ? payment.getNotes() + "\n" : "")
                    + "Cancelled: " + reason);
        }

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Successfully cancelled payment with ID: {}", id);

        return paymentMapper.toPaymentResponse(savedPayment);
    }

    /**
     * Refunds a payment.
     */
    @Transactional
    public PaymentResponse refundPayment(Long id, String reason) {
        log.info("Refunding payment with ID: {}", id);

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("messages.payment.notFound", id));

        // Business Rule: Can only refund completed payments
        if (payment.getStatus() != com.mahmoud.nagieb.modules.installments.payment.enums.PaymentStatus.COMPLETED) {
            throw new BusinessException("messages.payment.cannotRefundNonCompleted");
        }

        payment.setStatus(com.mahmoud.nagieb.modules.installments.payment.enums.PaymentStatus.REFUNDED);
        if (reason != null && !reason.isBlank()) {
            payment.setNotes((payment.getNotes() != null ? payment.getNotes() + "\n" : "")
                    + "Refunded: " + reason);
        }

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Successfully refunded payment with ID: {}", id);

        return paymentMapper.toPaymentResponse(savedPayment);
    }



    // ============== Helper Methods ==============

    /**
     * Calculate final discount amount combining automatic and manual discounts.
     * Implements requirement 7: “A mechanism for making a discount when paying early or in the last installment.”     */
    private BigDecimal calculateFinalDiscount(PaymentRequest request, InstallmentSchedule schedule) {
        if (request.getDiscountAmount() != null && request.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            log.info("Using manual discount amount: {}", request.getDiscountAmount());
            return request.getDiscountAmount();
        }

        if (schedule == null) {
            return BigDecimal.ZERO;
        }

        // Calculate automatic discounts
        LocalDate dueDate = schedule.getDueDate();
        boolean isFinalInstallment = isFinalInstallment(schedule);

        // return higher (final or early) discount
        BigDecimal automaticDiscount = discountService.calculateHigherDiscount(
                request.getAmount(),
                request.getActualPaymentDate(),
                dueDate,
                isFinalInstallment
        );

        log.info("Calculated automatic discount: {} for payment", automaticDiscount);
        return automaticDiscount;
    }

    /**
     * Create payment entity with all calculated fields.
     */
    private Payment createPaymentEntity(PaymentRequest request, InstallmentSchedule schedule, BigDecimal discount) {

        Payment payment = paymentMapper.toPayment(request);
        if(schedule != null){
           LocalDate dueDate = schedule.getDueDate();
           String agreedMonth = dueDate.format(MONTH_FORMAT);
              payment.setAgreedPaymentMonth(request.getAgreedPaymentMonth() != null
                     ? request.getAgreedPaymentMonth()
                     : agreedMonth);
        }
        payment.setPaymentDate(LocalDateTime.now());
        // TODO make it pending and update to completed after all processing is successful
        payment.setStatus(com.mahmoud.nagieb.modules.installments.payment.enums.PaymentStatus.COMPLETED);
        payment.setInstallmentSchedule(schedule);
        payment.setDiscountAmount(discount);
        payment.setNetAmount(request.getAmount().subtract(discount));

        // Determine if early payment
        if (request.getIsEarlyPayment() == null && schedule != null) {
            payment.setIsEarlyPayment(discountService.isEarlyPayment(request.getActualPaymentDate(), schedule.getDueDate()));
        } else {
            payment.setIsEarlyPayment(request.getIsEarlyPayment() != null ? request.getIsEarlyPayment() : false);
        }

        // Set user relationships
        setPaymentUserRelationships(payment, request);

        return payment;
    }

    /**
     * Set user relationships for payment tracking.
     */
    private void setPaymentUserRelationships(Payment payment, PaymentRequest request) {
        // Set received by user (TODO: get from security context)
        User currentUser = userRepository.findById(1L)
                .orElseThrow(() -> new ObjectNotFoundException("messages.user.notFound", 1L));
        payment.setReceivedBy(currentUser);

        // Set collector if provided
        if (request.getCollectorId() != null) {
            User collector = userRepository.findById(request.getCollectorId())
                    .orElseThrow(() -> new ObjectNotFoundException(
                            "messages.user.notFound", request.getCollectorId()));
            payment.setCollector(collector);
        }
    }

    /**
     * Update installment schedule status after payment.
     */
    private void updateInstallmentScheduleStatus(InstallmentSchedule schedule, Payment payment) {

        // Update schedule with payment information
        schedule.setPaidAmount(schedule.getPaidAmount().add(payment.getNetAmount()));
        schedule.setPaidDate(payment.getActualPaymentDate());

        // Determine new status
        if (schedule.getPaidAmount().compareTo(schedule.getAmount()) >= 0) {
            schedule.setStatus(com.mahmoud.nagieb.modules.installments.contract.enums.PaymentStatus.PAID);
        } else {
            schedule.setStatus(com.mahmoud.nagieb.modules.installments.contract.enums.PaymentStatus.PARTIALLY_PAID);
        }

        installmentScheduleRepository.save(schedule);
        log.info("Updated installment schedule {} status to {}", schedule.getId(), schedule.getStatus());
    }

    /**
     * Validate payment request business rules.
     */
    private void validatePaymentRequest(PaymentRequest request) {
        // Business Rule 1: Validate amount is positive
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("messages.payment.amountMustBePositive");
        }

        // Business Rule 3: Validate discount doesn't exceed payment amount
        if (request.getDiscountAmount() != null &&
                request.getDiscountAmount().compareTo(request.getAmount()) > 0) {
            throw new BusinessException("messages.payment.discountExceedsAmount");
        }
    }
    /**
     * Validates payment month format (yyyy-MM).
     */
    private void validatePaymentMonth(String month) {
        if (month == null || !month.matches("^\\d{4}-\\d{2}$")) {
            throw new BusinessException("messages.payment.invalidMonthFormat");
        }
        try {
            YearMonth.parse(month, MONTH_FORMAT);
        } catch (Exception e) {
            throw new BusinessException("messages.payment.invalidMonthFormat");
        }
    }


    /**
     * Check if this is the final installment for a contract.
     */
    private boolean isFinalInstallment(InstallmentSchedule schedule) {
        List<InstallmentSchedule> allSchedules = installmentScheduleRepository
                .findByContractIdOrderBySequenceNumberAsc(schedule.getContract().getId());

        return schedule.getSequenceNumber().equals(allSchedules.size());
    }
}
