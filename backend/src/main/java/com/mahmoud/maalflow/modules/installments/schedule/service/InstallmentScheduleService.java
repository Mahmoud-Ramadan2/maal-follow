package com.mahmoud.maalflow.modules.installments.schedule.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.exception.ObjectNotFoundException;
import com.mahmoud.maalflow.modules.installments.contract.dto.ContractExpenseRequest;
import com.mahmoud.maalflow.modules.installments.contract.service.ContractCompletionPolicy;
import com.mahmoud.maalflow.modules.installments.contract.service.ContractExpenseService;
import com.mahmoud.maalflow.modules.installments.schedule.dto.InstallmentScheduleRequest;
import com.mahmoud.maalflow.modules.installments.schedule.dto.InstallmentScheduleResponse;
import com.mahmoud.maalflow.modules.installments.contract.entity.Contract;
import com.mahmoud.maalflow.modules.installments.schedule.dto.MonthlyCollectionSummary;
import com.mahmoud.maalflow.modules.installments.schedule.dto.ScheduleParameters;
import com.mahmoud.maalflow.modules.installments.schedule.dto.ScheduleMetadataUpdateRequest;
import com.mahmoud.maalflow.modules.installments.schedule.entity.InstallmentSchedule;
import com.mahmoud.maalflow.modules.installments.contract.enums.ExpenseType;
import com.mahmoud.maalflow.modules.installments.contract.enums.PaymentStatus;
import com.mahmoud.maalflow.modules.installments.schedule.mapper.InstallmentScheduleMapper;
import com.mahmoud.maalflow.modules.installments.contract.repo.ContractRepository;
import com.mahmoud.maalflow.modules.installments.schedule.repo.InstallmentScheduleRepository;
import com.mahmoud.maalflow.modules.installments.ledger.dto.LedgerRequest;
import com.mahmoud.maalflow.modules.installments.ledger.enums.LedgerReferenceType;
import com.mahmoud.maalflow.modules.installments.ledger.enums.LedgerSource;
import com.mahmoud.maalflow.modules.installments.ledger.enums.LedgerType;
import com.mahmoud.maalflow.modules.installments.ledger.service.LedgerService;
import com.mahmoud.maalflow.modules.installments.payment.entity.Payment;
import com.mahmoud.maalflow.modules.installments.payment.enums.PaymentMethod;
import com.mahmoud.maalflow.modules.installments.payment.repo.PaymentRepository;
import com.mahmoud.maalflow.modules.installments.profit.service.ProfitProcessingService;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import com.mahmoud.maalflow.modules.shared.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.*;
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

import static com.mahmoud.maalflow.modules.shared.constants.AppConstants.MONTH_FORMAT;

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
    private final ProfitProcessingService profitProcessingService;
    private final ContractExpenseService contractExpenseService;
    private  final LedgerService ledgerService;
    private final ContractCompletionPolicy contractCompletionPolicy;
    private final ScheduleFactory scheduleFactory;
    private final ScheduleGenerationPolicy scheduleGenerationPolicy;
    private final ScheduleStatusStateMachine scheduleStatusStateMachine;




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
        ScheduleParameters params = scheduleGenerationPolicy.calculateScheduleParameters(
                totalAmount, numberOfMonths, monthlyAmount, contract.getMonths());

        // Update contract with the calculated/provided months and monthlyAmount
        if (numberOfMonths != null || monthlyAmount != null) {
            contract.setMonths(params.getMonths());
            contract.setMonthlyAmount(params.getMonthlyAmount());
            contractRepository.save(contract);
            log.info("Updated contract {} with months={} and monthlyAmount={}",
                    contractId, params.getMonths(), params.getMonthlyAmount());
        }

        // Generate collection list
        List<InstallmentSchedule> schedules = scheduleFactory.createScheduleList(
                contract, totalAmount, totalPrincipal, totalProfit,
                params.getMonths(), params.getMonthlyAmount(), putRemainderFirst);

        // Save all schedules
        scheduleRepository.saveAll(schedules);

        log.info("Generated {} installment schedules for contract {} (monthly: {}, remainder in {})",
                schedules.size(), contractId, params.getMonthlyAmount(), putRemainderFirst ? "first" : "last");

        return schedules.stream()
                .map(scheduleMapper::toResponse)
                .collect(Collectors.toList());
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
//    @Transactional
//    public void deleteUnpaidSchedules(Long contractId) {
//        List<InstallmentSchedule> schedules = scheduleRepository
//                .findByContractIdOrderBySequenceNumberAsc(contractId);
//
//        List<InstallmentSchedule> unpaidSchedules = schedules.stream()
//                .filter(s -> s.getStatus() == PaymentStatus.PENDING || s.getStatus() == PaymentStatus.LATE)
//                .collect(Collectors.toList());
//
//        if (!unpaidSchedules.isEmpty()) {
//            scheduleRepository.deleteAll(unpaidSchedules);
//            log.info("Deleted {} unpaid schedules for contract {}", unpaidSchedules.size(), contractId);
//        }
//    }

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
            scheduleStatusStateMachine.transition(s, PaymentStatus.CANCELLED);
            scheduleRepository.save(s);
        });

        // Calculate parameters for new schedules
        ScheduleParameters params =scheduleGenerationPolicy.calculateScheduleParameters(
                remainingAmount, newNumberOfMonths, newMonthlyAmount, contract.getMonths());

        // Create new schedules
        List<InstallmentSchedule> newSchedules = scheduleFactory.createScheduleList(
                contract, remainingAmount, remainingPrincipal, remainingProfit,
                params.getMonths(), params.getMonthlyAmount(), false);

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
     * Create a single installment collection manually
     */
    @Transactional
    public InstallmentScheduleResponse createSchedule(InstallmentScheduleRequest request) {
        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new ObjectNotFoundException("messages.contract.notFound", request.getContractId()));
        InstallmentSchedule schedule = scheduleMapper.toEntity(request);
        schedule.setContract(contract);
        schedule.setOriginalAmount(request.getAmount());
        // Manual schedule creation defaults to principal-only unless generated/rescheduled by business policies.
        schedule.setPrincipalAmount(request.getAmount());
        schedule.setProfitAmount(BigDecimal.ZERO);
        schedule.setPrincipalPaid(BigDecimal.ZERO);
        schedule.setProfitPaid(BigDecimal.ZERO);
        if (schedule.getPaidAmount() == null) {
            schedule.setPaidAmount(BigDecimal.ZERO);
        }
        if (schedule.getDiscountApplied() == null) {
            schedule.setDiscountApplied(BigDecimal.ZERO);
        }
        if (schedule.getStatus() == null) {
            schedule.setStatus(PaymentStatus.PENDING);
        } else {
            scheduleStatusStateMachine.validateTransition(PaymentStatus.PENDING, schedule.getStatus());
        }
        if (schedule.getProfitMonth() == null || schedule.getProfitMonth().isBlank()) {
            schedule.setProfitMonth(schedule.getDueDate().format(MONTH_FORMAT));
        }
        if (request.getCollectorId() != null) {
            User collector = userRepository.findById(request.getCollectorId())
                    .orElseThrow(() -> new ObjectNotFoundException("messages.user.notFound", request.getCollectorId()));
            schedule.setCollector(collector);
        }
        InstallmentSchedule saved = scheduleRepository.save(schedule);
        log.info("Created installment collection {} for contract {}", saved.getId(), contract.getId());
        return scheduleMapper.toResponse(saved);
    }

    /**
     * Update an existing installment collection
     */
    @Transactional
    public InstallmentScheduleResponse updateSchedule(Long id, InstallmentScheduleRequest request) {
        InstallmentSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("messages.schedule.notFound", id));
        if (request.getContractId() != null && !request.getContractId().equals(schedule.getContract().getId())) {
            throw new BusinessException("messages.schedule.contractCannotChange");
        }
        BigDecimal requestedAmount = request.getAmount();
        BigDecimal currentAmount = schedule.getAmount();
        boolean amountChanged = requestedAmount != null && currentAmount != null && requestedAmount.compareTo(currentAmount) != 0;
        BigDecimal requestedDiscount = request.getDiscountApplied();
        BigDecimal currentDiscount = safeDecimal(schedule.getDiscountApplied());
        boolean discountChanged = requestedDiscount != null && requestedDiscount.compareTo(currentDiscount) != 0;
        boolean statusChanged = request.getStatus() != null && request.getStatus() != schedule.getStatus();
        if (amountChanged || discountChanged || statusChanged) {
            throw new BusinessException("Direct financial/status updates are blocked. Use payment or reschedule endpoints.");
        }
        boolean dueDateChanged = request.getDueDate() != null && !request.getDueDate().equals(schedule.getDueDate());
        if (dueDateChanged && isFinanciallyLocked(schedule)) {
            throw new BusinessException("Cannot change due date after payment/discount activity on this schedule.");
        }
        if (dueDateChanged) {
            schedule.setDueDate(request.getDueDate());
            schedule.setProfitMonth(request.getDueDate().format(MONTH_FORMAT));
        }
        if (request.getNotes() != null) {
            schedule.setNotes(request.getNotes());
        }
        if (request.getCollectorId() != null) {
            User collector = userRepository.findById(request.getCollectorId())
                    .orElseThrow(() -> new ObjectNotFoundException("messages.user.notFound", request.getCollectorId()));
            schedule.setCollector(collector);
        }
        InstallmentSchedule saved = scheduleRepository.save(schedule);
        log.info("Updated installment collection {} with safe non-financial fields only", id);
        return scheduleMapper.toResponse(saved);
    }

    /**
     *  Get schedule by ID
     * @param id
     * @return
     */
    /**
     * Update non-financial metadata only.
     */
    @Transactional
    public InstallmentScheduleResponse updateScheduleMetadata(Long id, ScheduleMetadataUpdateRequest request) {
        InstallmentSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("messages.schedule.notFound", id));
        boolean dueDateChanged = request.getDueDate() != null && !request.getDueDate().equals(schedule.getDueDate());
        if (dueDateChanged && isFinanciallyLocked(schedule)) {
            throw new BusinessException("Cannot change due date after payment/discount activity on this schedule.");
        }
        if (dueDateChanged) {
            schedule.setDueDate(request.getDueDate());
            schedule.setProfitMonth(request.getDueDate().format(MONTH_FORMAT));
        }
        if (request.getNotes() != null) {
            schedule.setNotes(request.getNotes());
        }
        if (Boolean.TRUE.equals(request.getClearCollector())) {
            schedule.setCollector(null);
        } else if (request.getCollectorId() != null) {
            User collector = userRepository.findById(request.getCollectorId())
                    .orElseThrow(() -> new ObjectNotFoundException("messages.user.notFound", request.getCollectorId()));
            schedule.setCollector(collector);
        }
        InstallmentSchedule saved = scheduleRepository.save(schedule);
        log.info("Updated schedule metadata for {}", id);
        return scheduleMapper.toResponse(saved);
    }    public  InstallmentScheduleResponse getScheduleById(Long id) {
        return scheduleRepository.findById(id)
                .map(scheduleMapper::toResponse)
                .orElseThrow(() -> new ObjectNotFoundException("messages.schedule.notFound", id));
    }
    // ============== PAYMENT METHODS ==============


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
                .status(com.mahmoud.maalflow.modules.installments.payment.enums.PaymentProcessingStatus.COMPLETED)
                .paymentDate(LocalDateTime.now())
                .actualPaymentDate(paidDate)
                .isEarlyPayment(paidDate.isBefore(schedule.getDueDate()))
                .discountAmount(discount != null ? discount : BigDecimal.ZERO)
                .netAmount(amount)
                .receivedBy(receivedBy)
                .notes("تم دفع قسط برقم  #" + schedule.getSequenceNumber() + "للعقد رقم " + schedule.getContract().getContractNumber())
                .build();

        Payment saved = paymentRepository.save(payment);
        log.debug("Created payment record {} for collection {}", saved.getId(), schedule.getId());
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

    // ============== QUERY METHODS ==============

    /**
     * Get all schedules for a contract
     */
    public Page<InstallmentScheduleResponse> getSchedulesByContractId(Pageable pageable, Long contractId) {

//        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

        Page<InstallmentSchedule> schedules = scheduleRepository
                .findByContractIdOrderBySequenceNumberAsc(pageable, contractId);

        return schedules.map(scheduleMapper::toResponse);
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
     * Unified multi-filter paginated search for schedules.
     */
    public Page<InstallmentScheduleResponse> searchSchedules(
            Pageable pageable,
            Long contractId,
            PaymentStatus status,
            String name,
            Integer paymentDay,
            LocalDate startDate,
            LocalDate endDate,
            boolean overdueOnly,
            Integer dueSoonDays
    ) {
        LocalDate today = LocalDate.now();
        LocalDate dueSoonDate = (!overdueOnly && dueSoonDays != null && dueSoonDays > 0)
                ? today.plusDays(dueSoonDays)
                : null;

        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                    Sort.by(
                            Sort.Order.asc("dueDate")
                    ));
        }

        Page<InstallmentSchedule> schedules = scheduleRepository.searchSchedules(
                contractId,
                status,
                name,
                paymentDay,
                startDate,
                endDate,
                overdueOnly,
                today,
                dueSoonDate,
                pageable
        );

        return schedules.map(scheduleMapper::toResponse);
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
                .filter(s -> s.getStatus() == PaymentStatus.PENDING || s.getStatus() == PaymentStatus.LATE || s.getStatus() == PaymentStatus.PARTIALLY_PAID)
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

            LocalDate oldDate = schedule.getDueDate();
            schedule.setDueDate(newDueDate);
            schedule.setProfitMonth(newDueDate.format(MONTH_FORMAT));

            // Add note about the skip
//            String skipNote = "تم تأجيل هذا القسط بسبب: " + reason + " (بتاريخ: " + LocalDate.now() + ")";
            String skipNote = String.format(
                    " ترحيل القسط من تاريخ %s إلى %s بسبب: %s وذلك بتاريخ %s",
                    oldDate, schedule.getDueDate(), reason, LocalDate.now()
            );
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

    /**
     *
     * @param id
     * @return
     */
    public boolean existsPaidByContractId(Long id) {
//        boolean result = scheduleRepository.existsPaidByContractId(id);
        boolean exists = scheduleRepository.existsByContractIdAndStatusIn
                (id, List.of(PaymentStatus.PAID, PaymentStatus.PARTIALLY_PAID));
        return exists;
    }

    private BigDecimal safeDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
    private boolean isFinanciallyLocked(InstallmentSchedule schedule) {
        return safeDecimal(schedule.getPaidAmount()).compareTo(BigDecimal.ZERO) > 0
                || safeDecimal(schedule.getPrincipalPaid()).compareTo(BigDecimal.ZERO) > 0
                || safeDecimal(schedule.getProfitPaid()).compareTo(BigDecimal.ZERO) > 0
                || safeDecimal(schedule.getDiscountApplied()).compareTo(BigDecimal.ZERO) > 0
                || schedule.getStatus() == PaymentStatus.PAID
                || schedule.getStatus() == PaymentStatus.PARTIALLY_PAID
                || schedule.getStatus() == PaymentStatus.CANCELLED;
    }    // ============== HELPER CLASSES ==============


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

        log.debug("Payment ratio for collection {}: {}%, principal={}, profit={}",
                schedule.getId(), paymentRatio.multiply(BigDecimal.valueOf(100)), principalCollected, profitCollected);

        // لإTODO: These values could be stored in separate fields if needed for detailed tracking
        // For now, we're tracking the proportional amounts in the profit processing service
    }

}