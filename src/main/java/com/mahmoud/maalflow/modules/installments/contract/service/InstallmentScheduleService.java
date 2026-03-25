package com.mahmoud.maalflow.modules.installments.contract.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.exception.ObjectNotFoundException;
import com.mahmoud.maalflow.modules.installments.contract.dto.ContractExpenseRequest;
import com.mahmoud.maalflow.modules.installments.contract.dto.InstallmentScheduleRequest;
import com.mahmoud.maalflow.modules.installments.contract.dto.InstallmentScheduleResponse;
import com.mahmoud.maalflow.modules.installments.contract.entity.Contract;
import com.mahmoud.maalflow.modules.installments.contract.entity.InstallmentSchedule;
import com.mahmoud.maalflow.modules.installments.contract.enums.ContractStatus;
import com.mahmoud.maalflow.modules.installments.contract.enums.ExpenseType;
import com.mahmoud.maalflow.modules.installments.contract.enums.PaymentStatus;
import com.mahmoud.maalflow.modules.installments.contract.mapper.InstallmentScheduleMapper;
import com.mahmoud.maalflow.modules.installments.contract.repo.ContractRepository;
import com.mahmoud.maalflow.modules.installments.contract.repo.InstallmentScheduleRepository;
import com.mahmoud.maalflow.modules.installments.ledger.dto.LedgerRequest;
import com.mahmoud.maalflow.modules.installments.ledger.enums.LedgerReferenceType;
import com.mahmoud.maalflow.modules.installments.ledger.enums.LedgerSource;
import com.mahmoud.maalflow.modules.installments.ledger.enums.LedgerType;
import com.mahmoud.maalflow.modules.installments.ledger.service.LedgerService;
import com.mahmoud.maalflow.modules.installments.payment.entity.Payment;
import com.mahmoud.maalflow.modules.installments.payment.enums.PaymentMethod;
import com.mahmoud.maalflow.modules.installments.payment.repo.PaymentRepository;
import com.mahmoud.maalflow.modules.installments.payment.service.PaymentProcessingService;
import com.mahmoud.maalflow.modules.installments.profit.service.ProfitProcessingService;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import com.mahmoud.maalflow.modules.shared.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing installment schedules with proper rounding to multiples of 50.
 * @author Mahmoud
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InstallmentScheduleService {

    private final InstallmentScheduleRepository scheduleRepository;
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final InstallmentScheduleMapper scheduleMapper;
    private final PaymentRepository paymentRepository;
    private final PaymentProcessingService paymentProcessingService;
    private final ProfitProcessingService profitProcessingService;
    private final ContractExpenseService contractExpenseService;
    private  final LedgerService ledgerService;

    private static final BigDecimal MINIMUM_INSTALLMENT = BigDecimal.valueOf(50);
    private static final BigDecimal ROUNDING_UNIT = BigDecimal.valueOf(50);
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");


    // ============== SCHEDULE GENERATION METHODS ==============

    /**
     * Generate installment schedules for a contract (default: remainder in last installment)
     */
    @Transactional
    public List<InstallmentScheduleResponse> generateSchedulesForContract(Long contractId) {
        return generateSchedules(contractId, null, null, false);
    }

    /**
     * Generate installment schedules with custom parameter
     * @param contractId Contract ID
     * @param numberOfMonths Number of months (null to use contract's months)
     * @param monthlyAmount Monthly installment amount (null to calculate from numberOfMonths)
     * @param putRemainderFirst If true, put remainder in first installment; if false, put in last
     */
    @Transactional
    public List<InstallmentScheduleResponse> generateSchedules(
            Long contractId,
            Integer numberOfMonths,
            BigDecimal monthlyAmount,
            boolean putRemainderFirst) {

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ObjectNotFoundException("messages.contract.notFound", contractId));

        // Check if schedules already exist
        List<InstallmentSchedule> existingSchedules = scheduleRepository.findByContractIdOrderBySequenceNumberAsc(contractId);
        List<InstallmentSchedule> unpaidSchedules = existingSchedules.stream()
                .filter(s -> s.getStatus() != PaymentStatus.PAID && s.getStatus() != PaymentStatus.CANCELLED)
                .collect(Collectors.toList());

        if (!unpaidSchedules.isEmpty()) {
            throw new BusinessException("messages.schedule.alreadyExists");
        }

        // Calculate total amount to be financed (excluding down payment)
        BigDecimal totalPrincipal = contract.getOriginalPrice().subtract(contract.getDownPayment());
        BigDecimal totalProfit = contract.getProfitAmount() != null ? contract.getProfitAmount() : BigDecimal.ZERO;
        BigDecimal totalAmount = totalPrincipal.add(totalProfit);

        // Validate total amount
        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("messages.contract.invalidAmount");
        }

        // Determine number of months and monthly amount
        ScheduleParameters params = calculateScheduleParameters(
                totalAmount, numberOfMonths, monthlyAmount, contract.getMonths());

        // Update contract with the calculated/provided months and monthlyAmount
        if (numberOfMonths != null || monthlyAmount != null) {
            contract.setMonths(params.months);
            contract.setMonthlyAmount(params.monthlyAmount);
            contractRepository.save(contract);
            log.info("Updated contract {} with months={} and monthlyAmount={}",
                    contractId, params.months, params.monthlyAmount);
        }

        // Generate schedule list
        List<InstallmentSchedule> schedules = createScheduleList(
                contract, totalAmount, totalPrincipal, totalProfit,
                params.months, params.monthlyAmount, putRemainderFirst);

        // Save all schedules
        scheduleRepository.saveAll(schedules);

        log.info("Generated {} installment schedules for contract {} (monthly: {}, remainder in {})",
                schedules.size(), contractId, params.monthlyAmount, putRemainderFirst ? "first" : "last");

        return schedules.stream()
                .map(scheduleMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Calculate schedule parameters (number of months and monthly amount)
     */
    private ScheduleParameters calculateScheduleParameters(
            BigDecimal totalAmount,
            Integer numberOfMonths,
            BigDecimal monthlyAmount,
            Integer contractMonths) {

        // Case 1: User provides both months and amount - validate they match
        if (numberOfMonths != null && monthlyAmount != null) {
            validateMonthsAndAmount(totalAmount, numberOfMonths, monthlyAmount);
            return new ScheduleParameters(numberOfMonths, roundToMultipleOf50(monthlyAmount));
        }

        // Case 2: User provides monthly amount - calculate number of months
        if (monthlyAmount != null && monthlyAmount.compareTo(BigDecimal.ZERO) > 0) {
           // BigDecimal roundedAmount = roundToMultipleOf50(monthlyAmount);
            int calculatedMonths = calculateMonthsFromAmount(totalAmount, monthlyAmount);
            return new ScheduleParameters(calculatedMonths, monthlyAmount);
        }

        // Case 3: User provides number of months - calculate monthly amount
        if (numberOfMonths != null) {
            BigDecimal calculatedAmount = calculateAmountFromMonths(totalAmount, numberOfMonths);
            return new ScheduleParameters(numberOfMonths, calculatedAmount);
        }

        // Case 4: Use contract's default months
        if (contractMonths != null && contractMonths > 0) {
            BigDecimal calculatedAmount = calculateAmountFromMonths(totalAmount, contractMonths);
            return new ScheduleParameters(contractMonths, calculatedAmount);
        }

        throw new BusinessException("messages.contract.invalidMonths");
    }

    /**
     * Calculate rounded monthly amount from total amount and number of months
     */
    private BigDecimal calculateAmountFromMonths(BigDecimal totalAmount, int months) {
        if (months <= 0) {
            throw new BusinessException("messages.contract.invalidMonths");
        }

        // If total is less than 50, return total as single payment
        if (totalAmount.compareTo(MINIMUM_INSTALLMENT) < 0) {
            return totalAmount;
        }

        // Calculate average and round down to multiple of 50
        BigDecimal average = totalAmount.divide(BigDecimal.valueOf(months), 2, RoundingMode.DOWN);
        BigDecimal rounded = roundToMultipleOf50(average);

        // Ensure minimum installment of 50
        if (rounded.compareTo(MINIMUM_INSTALLMENT) < 0) {
            rounded = MINIMUM_INSTALLMENT;
        }

        return rounded;
    }

    /**
     * Calculate number of months from total amount and monthly installment
     */
    private int calculateMonthsFromAmount(BigDecimal totalAmount, BigDecimal monthlyAmount) {
        if (monthlyAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("messages.contract.invalidMonthlyAmount");
        }

        if (monthlyAmount.compareTo(totalAmount) > 0) {
            throw new BusinessException("messages.contract.monthlyAmountExceedsTotal");
        }

        // Calculate how many full installments are needed
        BigDecimal months = totalAmount.divide(monthlyAmount, 0, RoundingMode.UP);
        int monthsInt = months.intValue();

        // Ensure at least 1 month
        return Math.max(1, monthsInt);
    }

    /**
     * Validate that provided months and amount are compatible
     */
    private void validateMonthsAndAmount(BigDecimal totalAmount, int months, BigDecimal monthlyAmount) {
        if (months <= 0) {
            throw new BusinessException("messages.contract.invalidMonths");
        }

        BigDecimal maxTotal = monthlyAmount.multiply(BigDecimal.valueOf(months));
        if (maxTotal.compareTo(totalAmount) < 0) {
            throw new BusinessException("messages.contract.monthsAmountMismatch");
        }
    }

    /**
     * Create list of installment schedules
     */
    private List<InstallmentSchedule> createScheduleList(
            Contract contract,
            BigDecimal totalAmount,
            BigDecimal totalPrincipal,
            BigDecimal totalProfit,
            int months,
            BigDecimal roundedMonthlyAmount,
            boolean putRemainderFirst) {

        List<InstallmentSchedule> schedules = new ArrayList<>();
        LocalDate currentDueDate = contract.getStartDate();

        // Calculate regular installments and remainder
        BigDecimal regularTotal = roundedMonthlyAmount.multiply(BigDecimal.valueOf(months - 1));
        BigDecimal remainderAmount = totalAmount.subtract(regularTotal);

        // Ensure remainder amount is positive and reasonable
        if (remainderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            // Recalculate with lower monthly amount
            roundedMonthlyAmount = roundToMultipleOf50(
                    totalAmount.divide(BigDecimal.valueOf(months), 2, RoundingMode.DOWN));
            if (roundedMonthlyAmount.compareTo(MINIMUM_INSTALLMENT) < 0) {
                roundedMonthlyAmount = MINIMUM_INSTALLMENT;
            }
            regularTotal = roundedMonthlyAmount.multiply(BigDecimal.valueOf(months - 1));
            remainderAmount = totalAmount.subtract(regularTotal);
        }

        // Track accumulated principal and profit for proportional distribution
        BigDecimal accumulatedPrincipal = BigDecimal.ZERO;
        BigDecimal accumulatedProfit = BigDecimal.ZERO;

        for (int i = 1; i <= months; i++) {
            // Set due date to agreed payment day
            currentDueDate = currentDueDate.withDayOfMonth(
                    Math.min(contract.getAgreedPaymentDay(), currentDueDate.lengthOfMonth())
            );

            BigDecimal installmentAmount;
            BigDecimal principalAmount;
            BigDecimal profitAmount;
            boolean isFinal = (i == months);
            boolean isFirst = (i == 1);

            // Determine which installment gets the remainder
            boolean isRemainderInstallment = putRemainderFirst ? isFirst : isFinal;

            if (isFinal) {
                // Final installment ALWAYS uses subtraction to ensure exact totals
                installmentAmount = totalAmount.subtract(
                        roundedMonthlyAmount.multiply(BigDecimal.valueOf(months - 1)));
                if (putRemainderFirst) {
                    // If remainder was first, recalculate final as regular
                    installmentAmount = totalAmount.subtract(remainderAmount)
                            .subtract(roundedMonthlyAmount.multiply(BigDecimal.valueOf(months - 2)));
                }
                principalAmount = totalPrincipal.subtract(accumulatedPrincipal);
                profitAmount = totalProfit.subtract(accumulatedProfit);
            } else if (isRemainderInstallment) {
                // First installment gets the remainder (when putRemainderFirst = true)
                installmentAmount = remainderAmount;
                BigDecimal ratio = installmentAmount.divide(totalAmount, 10, RoundingMode.HALF_UP);
                principalAmount = totalPrincipal.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
                profitAmount = totalProfit.multiply(ratio).setScale(2, RoundingMode.HALF_UP);

                accumulatedPrincipal = accumulatedPrincipal.add(principalAmount);
                accumulatedProfit = accumulatedProfit.add(profitAmount);
            } else {
                // Regular installment with proportional distribution
                installmentAmount = roundedMonthlyAmount;
                BigDecimal ratio = installmentAmount.divide(totalAmount, 10, RoundingMode.HALF_UP);
                principalAmount = totalPrincipal.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
                profitAmount = totalProfit.multiply(ratio).setScale(2, RoundingMode.HALF_UP);

                accumulatedPrincipal = accumulatedPrincipal.add(principalAmount);
                accumulatedProfit = accumulatedProfit.add(profitAmount);
            }

            InstallmentSchedule schedule = InstallmentSchedule.builder()
                    .contract(contract)
                    .sequenceNumber(i)
                    .dueDate(currentDueDate)
                    .amount(installmentAmount)
                    .originalAmount(installmentAmount)
                    .principalAmount(principalAmount)
                    .profitAmount(profitAmount)
                    .profitMonth(currentDueDate.format(MONTH_FORMAT))
                    .status(PaymentStatus.PENDING)
                    .isFinalPayment(isFinal)
                    .discountApplied(BigDecimal.ZERO)
                    .paidAmount(BigDecimal.ZERO)
                    .build();

            schedules.add(schedule);

            // Move to next month
            currentDueDate = currentDueDate.plusMonths(1);
        }

        return schedules;
    }

    /**
     * Round amount to nearest multiple of 50 (round down)
     */
    private BigDecimal roundToMultipleOf50(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal divided = amount.divide(ROUNDING_UNIT, 0, RoundingMode.DOWN);
        BigDecimal rounded = divided.multiply(ROUNDING_UNIT);

        // If rounded is 0 but amount > 0, return 50
        if (rounded.compareTo(BigDecimal.ZERO) == 0 && amount.compareTo(BigDecimal.ZERO) > 0) {
            return ROUNDING_UNIT;
        }

        return rounded;
    }

    /**
     * Swap remainder amount between first and last installment
     */
    @Transactional
    public List<InstallmentScheduleResponse> swapRemainderPosition(Long contractId) {
        List<InstallmentSchedule> schedules = scheduleRepository
                .findByContractIdOrderBySequenceNumberAsc(contractId);

        if (schedules.isEmpty()) {
            throw new BusinessException("messages.schedule.noSchedulesToSwap");
        }

        // Filter only pending schedules (not paid or cancelled)
        List<InstallmentSchedule> pendingSchedules = schedules.stream()
                .filter(s -> s.getStatus() == PaymentStatus.PENDING || s.getStatus() == PaymentStatus.LATE)
                .collect(Collectors.toList());

        if (pendingSchedules.size() < 2) {
            throw new BusinessException("messages.schedule.insufficientSchedulesToSwap");
        }

        InstallmentSchedule firstSchedule = pendingSchedules.get(0);
        InstallmentSchedule lastSchedule = pendingSchedules.get(pendingSchedules.size() - 1);

        // Swap the amounts
        BigDecimal tempAmount = firstSchedule.getAmount();
        BigDecimal tempOriginalAmount = firstSchedule.getOriginalAmount();
        BigDecimal tempPrincipal = firstSchedule.getPrincipalAmount();
        BigDecimal tempProfit = firstSchedule.getProfitAmount();

        firstSchedule.setAmount(lastSchedule.getAmount());
        firstSchedule.setOriginalAmount(lastSchedule.getOriginalAmount());
        firstSchedule.setPrincipalAmount(lastSchedule.getPrincipalAmount());
        firstSchedule.setProfitAmount(lastSchedule.getProfitAmount());

        lastSchedule.setAmount(tempAmount);
        lastSchedule.setOriginalAmount(tempOriginalAmount);
        lastSchedule.setPrincipalAmount(tempPrincipal);
        lastSchedule.setProfitAmount(tempProfit);

        scheduleRepository.save(firstSchedule);
        scheduleRepository.save(lastSchedule);

        log.info("Swapped remainder between first and last installment for contract {}", contractId);

        return schedules.stream()
                .map(scheduleMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Delete all unpaid schedules for a contract (for regeneration)
     */
    @Transactional
    public void deleteUnpaidSchedules(Long contractId) {
        List<InstallmentSchedule> schedules = scheduleRepository
                .findByContractIdOrderBySequenceNumberAsc(contractId);

        List<InstallmentSchedule> unpaidSchedules = schedules.stream()
                .filter(s -> s.getStatus() == PaymentStatus.PENDING || s.getStatus() == PaymentStatus.LATE)
                .collect(Collectors.toList());

        if (!unpaidSchedules.isEmpty()) {
            scheduleRepository.deleteAll(unpaidSchedules);
            log.info("Deleted {} unpaid schedules for contract {}", unpaidSchedules.size(), contractId);
        }
    }

    // ============== RESCHEDULE METHODS ==============

    /**
     * Reschedule unpaid installments with new parameters
     */
    @Transactional
    public List<InstallmentScheduleResponse> rescheduleUnpaidInstallments(
            Long contractId,
            Integer newNumberOfMonths,
            BigDecimal newMonthlyAmount,
            LocalDate newStartDate) {

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ObjectNotFoundException("messages.contract.notFound", contractId));

        List<InstallmentSchedule> allSchedules = scheduleRepository.findByContractId(contractId);

        if (allSchedules.isEmpty()) {
            throw new BusinessException("messages.schedule.noSchedulesToReschedule");
        }

        // Separate paid and unpaid schedules
        List<InstallmentSchedule> paidSchedules = allSchedules.stream()
                .filter(s -> s.getStatus() == PaymentStatus.PAID || s.getStatus() == PaymentStatus.PARTIALLY_PAID)
                .collect(Collectors.toList());

        List<InstallmentSchedule> unpaidSchedules = allSchedules.stream()
                .filter(s -> s.getStatus() == PaymentStatus.PENDING || s.getStatus() == PaymentStatus.LATE)
                .collect(Collectors.toList());

        if (unpaidSchedules.isEmpty()) {
            throw new BusinessException("messages.schedule.noUnpaid");
        }

        // Calculate remaining amount
        BigDecimal totalPaid = paidSchedules.stream()
                .map(s -> s.getPaidAmount() != null ? s.getPaidAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPrincipal = contract.getOriginalPrice().subtract(contract.getDownPayment());
        BigDecimal totalProfit = contract.getProfitAmount() != null ? contract.getProfitAmount() : BigDecimal.ZERO;
        BigDecimal totalAmount = totalPrincipal.add(totalProfit);
        BigDecimal remainingAmount = totalAmount.subtract(totalPaid);

        if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("messages.schedule.nothingToReschedule");
        }

        // Calculate remaining principal and profit
        BigDecimal paidPrincipal = paidSchedules.stream()
                .map(s -> s.getPrincipalAmount() != null ? s.getPrincipalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal paidProfit = paidSchedules.stream()
                .map(s -> s.getProfitAmount() != null ? s.getProfitAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remainingPrincipal = totalPrincipal.subtract(paidPrincipal);
        BigDecimal remainingProfit = totalProfit.subtract(paidProfit);

        // Cancel old unpaid schedules
        unpaidSchedules.forEach(s -> {
            s.setStatus(PaymentStatus.CANCELLED);
            scheduleRepository.save(s);
        });

        // Calculate parameters for new schedules
        ScheduleParameters params = calculateScheduleParameters(
                remainingAmount, newNumberOfMonths, newMonthlyAmount, contract.getMonths());

        // Create new schedules
        List<InstallmentSchedule> newSchedules = createScheduleList(
                contract, remainingAmount, remainingPrincipal, remainingProfit,
                params.months, params.monthlyAmount, false);

        // Update due dates to start from newStartDate
        if (newStartDate == null) {
            newStartDate = contract.getStartDate();
        }
        LocalDate currentDate = newStartDate;
        for (InstallmentSchedule schedule : newSchedules) {
            currentDate = currentDate.withDayOfMonth(
                    Math.min(contract.getAgreedPaymentDay(), currentDate.lengthOfMonth())
            );
            schedule.setDueDate(currentDate);
            schedule.setProfitMonth(currentDate.format(MONTH_FORMAT));
            currentDate = currentDate.plusMonths(1);
        }

        scheduleRepository.saveAll(newSchedules);

        log.info("Rescheduled {} unpaid installments for contract {}", newSchedules.size(), contractId);

        // Return all schedules (paid + new)
        List<InstallmentSchedule> allUpdated = scheduleRepository
                .findByContractIdOrderBySequenceNumberAsc(contractId);

        return allUpdated.stream()
                .filter(s -> s.getStatus() != PaymentStatus.CANCELLED)
                .map(scheduleMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ============== CRUD METHODS ==============

    /**
     * Create a single installment schedule manually
     */
    @Transactional
    public InstallmentScheduleResponse createSchedule(InstallmentScheduleRequest request) {
        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new ObjectNotFoundException("messages.contract.notFound", request.getContractId()));

        InstallmentSchedule schedule = scheduleMapper.toEntity(request);
        schedule.setContract(contract);
        schedule.setOriginalAmount(request.getAmount());

        if (schedule.getPaidAmount() == null) {
            schedule.setPaidAmount(BigDecimal.ZERO);
        }

        if (request.getCollectorId() != null) {
            User collector = userRepository.findById(request.getCollectorId())
                    .orElseThrow(() -> new ObjectNotFoundException("messages.user.notFound", request.getCollectorId()));
            schedule.setCollector(collector);
        }

        InstallmentSchedule saved = scheduleRepository.save(schedule);
        log.info("Created installment schedule {} for contract {}", saved.getId(), contract.getId());

        return scheduleMapper.toResponse(saved);
    }

    /**
     * Update an existing installment schedule
     */
    @Transactional
    public InstallmentScheduleResponse updateSchedule(Long id, InstallmentScheduleRequest request) {
        InstallmentSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("messages.schedule.notFound", id));

        if (request.getAmount() != null) {
            schedule.setAmount(request.getAmount());
        }
        if (request.getDueDate() != null) {
            schedule.setDueDate(request.getDueDate());
            schedule.setProfitMonth(request.getDueDate().format(MONTH_FORMAT));
        }
        if (request.getStatus() != null) {
            schedule.setStatus(request.getStatus());
        }
        if (request.getNotes() != null) {
            schedule.setNotes(request.getNotes());
        }
        if (request.getDiscountApplied() != null) {
            schedule.setDiscountApplied(request.getDiscountApplied());
        }
        if (request.getCollectorId() != null) {
            User collector = userRepository.findById(request.getCollectorId())
                    .orElseThrow(() -> new ObjectNotFoundException("messages.user.notFound", request.getCollectorId()));
            schedule.setCollector(collector);
        }

        InstallmentSchedule saved = scheduleRepository.save(schedule);
        log.info("Updated installment schedule {}", id);

        return scheduleMapper.toResponse(saved);
    }

    // ============== PAYMENT METHODS ==============

    /**
     * @deprecated Use PaymentService.processPayment() instead.
     * This method is kept for backward compatibility but should not be called directly.
     * PaymentService.processPayment() is the SINGLE ENTRY POINT that handles:
     * - Payment record creation
     * - Daily ledger (cash tracking)
     * - Capital return (principal portion)
     * - Expense recording (ContractExpense, NOT Deduction)
     * - Profit calculation
     * - Schedule/Contract updates
     */
    @Deprecated
    @Transactional
    public InstallmentScheduleResponse markAsPaid(
            Long scheduleId,
            BigDecimal paidAmount,
            LocalDate paidDate,
            BigDecimal discount,
            BigDecimal expense) {

        InstallmentSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ObjectNotFoundException("messages.schedule.notFound", scheduleId));

        // Validations
        if (schedule.getStatus() == PaymentStatus.CANCELLED) {
            throw new BusinessException("messages.schedule.canceledCannotPay");
        }
        if (schedule.getStatus() == PaymentStatus.PAID) {
            throw new BusinessException("messages.schedule.alreadyPaid");
        }
        if (paidAmount == null || paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("messages.payment.invalidAmount");
        }

        Contract contract = schedule.getContract();

        // Apply discount if provided
        if (discount != null && discount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal currentDiscount = schedule.getDiscountApplied() != null
                    ? schedule.getDiscountApplied() : BigDecimal.ZERO;
            schedule.setDiscountApplied(currentDiscount.add(discount));
        }

        // Calculate amounts
        BigDecimal totalDiscount = schedule.getDiscountApplied() != null
                ? schedule.getDiscountApplied() : BigDecimal.ZERO;
        BigDecimal currentPaid = schedule.getPaidAmount() != null
                ? schedule.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal amountDue = schedule.getAmount().subtract(totalDiscount).subtract(currentPaid);

        // Calculate actual payment (don't over-pay this schedule)
        BigDecimal actualPayment = paidAmount.min(amountDue);

        // Update schedule payment information
        if (paidDate == null) {
            paidDate = LocalDate.now();
        }
        schedule.setPaidAmount(currentPaid.add(actualPayment));
        schedule.setPaidDate(paidDate);

        // Determine payment status
        boolean isFullyPaid = schedule.getPaidAmount().compareTo(
                schedule.getAmount().subtract(totalDiscount)) >= 0;

        if (isFullyPaid) {
            schedule.setStatus(PaymentStatus.PAID);
        } else {
            schedule.setStatus(PaymentStatus.PARTIALLY_PAID);
        }

        InstallmentSchedule saved = scheduleRepository.save(schedule);

        // Get current user (TODO: get from security context)
        User currentUser = userRepository.findById(1L)
                .orElseThrow(() -> new ObjectNotFoundException("messages.user.notFound", 1L));

        // 1.add schedule expense

        if (expense.compareTo(BigDecimal.ZERO)> 0){
            addExpenseToSchedule(contract.getId(), schedule.getId(), expense,
                    "مصاريف اضافيه للقسط رقم " + schedule.getId() + "الخاص بالعقد رقم " + schedule.getContract().getId());
        }
        // 1. Create a Payment record
        Payment payment = createPaymentRecord(saved, actualPayment, paidDate, discount, currentUser);

        // 2. Record income in daily ledger (cash account)
        LedgerRequest ledgerRequest = LedgerRequest.builder()
                        .idempotencyKey("LEDGER-INSTALLMENT" + payment.getId())
                        .type(LedgerType.INCOME)
                        .amount(actualPayment)
                        .source(LedgerSource.COLLECTION)
                        .referenceType(LedgerReferenceType.INSTALLMENT_SCHEDULE)
                        .referenceId(saved.getId())
                .description("قسط داخل رقم: " + saved.getSequenceNumber() +
                        " - خاص بعقد رقم: " + contract.getId())
                        .date(paidDate)
                        .build();


        ledgerService.createLedgerEntry(ledgerRequest);


        // 3. Process proportional profit for ANY payment (full or partial)
        profitProcessingService.processProportionalInstallmentProfit(saved, actualPayment, paidDate, currentUser);

        // 4. Update paid principal and profit amounts on the schedule
        updatePaidPrincipalAndProfit(saved, actualPayment);

        // 5. Update contract remaining amount
        updateContractRemainingAmount(contract);

        // 6. Check if contract is completed
        checkAndCompleteContract(contract);

        // 7. Handle overpayment - apply to next schedule
        BigDecimal overpayment = paidAmount.subtract(actualPayment);
        if (overpayment.compareTo(BigDecimal.ZERO) > 0) {
            //applyOverpaymentToNextSchedule(contract.getId(), overpayment, payment, currentUser);
        }

        log.info("Marked installment schedule {} as {} with amount {} (discount: {})",
                scheduleId, saved.getStatus(), actualPayment, discount);

        return scheduleMapper.toResponse(saved);
    }

    /**
     * Create Payment record for audit trail
     */
    private Payment createPaymentRecord(
            InstallmentSchedule schedule,
            BigDecimal amount,
            LocalDate paidDate,
            BigDecimal discount,
            User receivedBy) {

        String idempotencyKey = "PAY-" + schedule.getId() + "-" + paidDate + "-" + UUID.randomUUID().toString().substring(0, 8);

        Payment payment = Payment.builder()
                .idempotencyKey(idempotencyKey)
                .installmentSchedule(schedule)
                .amount(amount)
                .paymentMethod(PaymentMethod.CASH)
                .status(com.mahmoud.maalflow.modules.installments.payment.enums.PaymentStatus.COMPLETED)
                .paymentDate(LocalDateTime.now())
                .actualPaymentDate(paidDate)
                .isEarlyPayment(paidDate.isBefore(schedule.getDueDate()))
                .discountAmount(discount != null ? discount : BigDecimal.ZERO)
                .netAmount(amount)
                .receivedBy(receivedBy)
                .notes("تم دفع قسط برقم  #" + schedule.getSequenceNumber() + "للعقد رقم " + schedule.getContract().getContractNumber())
                .build();

        Payment saved = paymentRepository.save(payment);
        log.debug("Created payment record {} for schedule {}", saved.getId(), schedule.getId());
        return saved;
    }

    /**
     * Update contract remaining amount based on paid schedules
     */
    private void updateContractRemainingAmount(Contract contract) {
        List<InstallmentSchedule> allSchedules = scheduleRepository.findByContractId(contract.getId());

        BigDecimal totalPaid = allSchedules.stream()
                .map(s -> s.getPaidAmount() != null ? s.getPaidAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDiscount = allSchedules.stream()
                .map(s -> s.getDiscountApplied() != null ? s.getDiscountApplied() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPrincipal = contract.getOriginalPrice().subtract(contract.getDownPayment());
        BigDecimal totalProfit = contract.getProfitAmount() != null ? contract.getProfitAmount() : BigDecimal.ZERO;
        BigDecimal totalAmount = totalPrincipal.add(totalProfit);

        BigDecimal remaining = totalAmount.subtract(totalPaid).subtract(totalDiscount);
        contract.setRemainingAmount(remaining.max(BigDecimal.ZERO));
        contractRepository.save(contract);

        log.debug("Updated contract {} remaining amount to {}", contract.getId(), remaining);
    }

    /**
     * Check if all schedules are paid and mark contract as completed
     */
    private void checkAndCompleteContract(Contract contract) {
        Long pendingCount = scheduleRepository.countPendingByContractId(contract.getId());

        if (pendingCount == 0) {
            contract.setStatus(ContractStatus.COMPLETED);
            contract.setCompletionDate(LocalDate.now());
            contractRepository.save(contract);
            log.info("Contract {} marked as completed", contract.getId());
        }
    }

    // ============== QUERY METHODS ==============

    /**
     * Get all schedules for a contract
     */
    public List<InstallmentScheduleResponse> getSchedulesByContractId(Long contractId) {
        List<InstallmentSchedule> schedules = scheduleRepository
                .findByContractIdOrderBySequenceNumberAsc(contractId);

        return schedules.stream()
                .map(scheduleMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get overdue schedules
     */
    public List<InstallmentScheduleResponse> getOverdueSchedules() {
        List<InstallmentSchedule> schedules = scheduleRepository.findOverdueSchedules(LocalDate.now());

        return schedules.stream()
                .map(scheduleMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get schedules due soon (within N days)
     */
    public List<InstallmentScheduleResponse> getSchedulesDueSoon(int daysAhead) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(daysAhead);

        List<InstallmentSchedule> schedules = scheduleRepository.findSchedulesDueBetween(
                startDate, endDate, PaymentStatus.PENDING);

        return schedules.stream()
                .map(scheduleMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get schedules by payment day
     */
    public List<InstallmentScheduleResponse> getSchedulesByPaymentDay(Integer paymentDay) {
        List<InstallmentSchedule> schedules = scheduleRepository
                .findByAgreedPaymentDayAndPending(paymentDay);

        return schedules.stream()
                .map(scheduleMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get schedules by customer name
     */
    public List<InstallmentScheduleResponse> getSchedulesByName(String name) {
        List<InstallmentSchedule> schedules = scheduleRepository.findPendingByCustomerName(name);

        return schedules.stream()
                .map(scheduleMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get schedules by status
     */
    public Page<InstallmentScheduleResponse> getSchedulesByStatus(PaymentStatus status, Pageable pageable) {
        Page<InstallmentSchedule> schedules = scheduleRepository.findByStatus(status, pageable);

        List<InstallmentScheduleResponse> responses = schedules.stream()
                .map(scheduleMapper::toResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, schedules.getTotalElements());
    }

    /**
     * Get monthly collection summary
     */
    public MonthlyCollectionSummary getMonthlyCollectionSummary(String monthYear) {
        BigDecimal expected = scheduleRepository.getTotalExpectedForMonth(monthYear);
        BigDecimal actual = scheduleRepository.getTotalPaidForMonth(monthYear);

        return new MonthlyCollectionSummary(monthYear, expected, actual,
                expected != null && actual != null ? expected.subtract(actual) : BigDecimal.ZERO);
    }

    /**
     * Skip payment for a month (e.g., Ramadan)
     */
    @Transactional
    public void skipMonthPayment(Long contractId, String reason) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ObjectNotFoundException("messages.contract.notFound", contractId));

        List<InstallmentSchedule> schedules = scheduleRepository
                .findByContractIdOrderBySequenceNumberAsc(contractId);

        List<InstallmentSchedule> unpaidSchedules = schedules.stream()
                .filter(s -> s.getStatus() == PaymentStatus.PENDING || s.getStatus() == PaymentStatus.LATE)
                .collect(Collectors.toList());

        if (unpaidSchedules.isEmpty()) {
            throw new BusinessException("messages.schedule.noUnpaidToSkip");
        }

        // Postpone all unpaid installments by 1 month
        for (InstallmentSchedule schedule : unpaidSchedules) {
            LocalDate newDueDate = schedule.getDueDate().plusMonths(1);

            // Maintain the agreed payment day
            newDueDate = newDueDate.withDayOfMonth(
                    Math.min(contract.getAgreedPaymentDay(), newDueDate.lengthOfMonth())
            );

            schedule.setDueDate(newDueDate);
            schedule.setProfitMonth(newDueDate.format(MONTH_FORMAT));

            // Add note about the skip
            String skipNote = "تم تأجيل هذا القسط بسبب: " + reason + " (بتاريخ: " + LocalDate.now() + ")";
            String currentNotes = schedule.getNotes();
            if (currentNotes != null && !currentNotes.isEmpty()) {
                schedule.setNotes(currentNotes + " | " + skipNote);
            } else {
                schedule.setNotes(skipNote);
            }

            scheduleRepository.save(schedule);
        }

        log.info("Skipped payment for contract {} - {} unpaid installments postponed by 1 month. Reason: {}",
                contractId, unpaidSchedules.size(), reason);
    }

    public void addExpenseToSchedule(Long contractId, Long scheduleId, BigDecimal expenseAmount, String notes) {

        ContractExpenseRequest request =
                ContractExpenseRequest.builder()
                        .contractId(contractId)
                        .expenseType(ExpenseType.INSTALLMENT)
                        .scheduleId(scheduleId)
                        .amount(expenseAmount)
                        .expenseDate(LocalDate.now())
                        .notes(notes)
                        .build();
        contractExpenseService.createExpense(request);
    }

    // ============== HELPER CLASSES ==============


    /**
     * DTO for schedule parameters
     */
    private static class ScheduleParameters {
        int months;
        BigDecimal monthlyAmount;

        ScheduleParameters(int months, BigDecimal monthlyAmount) {
            this.months = months;
            this.monthlyAmount = monthlyAmount;
        }
    }

    /**
     * DTO for monthly collection summary
     */
    public record MonthlyCollectionSummary(
            String month,
            BigDecimal expectedAmount,
            BigDecimal actualAmount,
            BigDecimal shortfall
    ) {}

    /**
     * Update paid principal and profit amounts based on payment ratio
     * This tracks how much of the principal vs profit has been collected
     */
    private void updatePaidPrincipalAndProfit(InstallmentSchedule schedule, BigDecimal paidAmount) {
        BigDecimal totalAmount = schedule.getAmount();
        BigDecimal totalPrincipal = schedule.getPrincipalAmount();
        BigDecimal totalProfit = schedule.getProfitAmount();

        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        // Calculate payment ratio
        BigDecimal paymentRatio = paidAmount.divide(totalAmount, 10, RoundingMode.HALF_UP);

        // Calculate proportional principal and profit collected
        BigDecimal principalCollected = totalPrincipal.multiply(paymentRatio).setScale(2, RoundingMode.HALF_UP);
        BigDecimal profitCollected = totalProfit.multiply(paymentRatio).setScale(2, RoundingMode.HALF_UP);

        log.debug("Payment ratio for schedule {}: {}%, principal={}, profit={}",
                schedule.getId(), paymentRatio.multiply(BigDecimal.valueOf(100)), principalCollected, profitCollected);

        // لإTODO: These values could be stored in separate fields if needed for detailed tracking
        // For now, we're tracking the proportional amounts in the profit processing service
    }
}
