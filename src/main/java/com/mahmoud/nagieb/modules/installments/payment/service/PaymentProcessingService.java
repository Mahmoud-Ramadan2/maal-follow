package com.mahmoud.nagieb.modules.installments.payment.service;

import com.mahmoud.nagieb.modules.installments.capital.service.CapitalService;
import com.mahmoud.nagieb.modules.installments.contract.entity.Contract;
import com.mahmoud.nagieb.modules.installments.contract.entity.InstallmentSchedule;
import com.mahmoud.nagieb.modules.installments.contract.enums.ContractStatus;
import com.mahmoud.nagieb.modules.installments.contract.enums.PaymentStatus;
import com.mahmoud.nagieb.modules.installments.contract.repo.ContractRepository;
import com.mahmoud.nagieb.modules.installments.contract.repo.InstallmentScheduleRepository;
import com.mahmoud.nagieb.modules.installments.payment.entity.Payment;
import com.mahmoud.nagieb.modules.installments.profit.service.ProfitProcessingService;
import com.mahmoud.nagieb.modules.shared.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Core service for processing installment payments.
 * This service is extracted to break the circular dependency between
 * PaymentService and InstallmentScheduleService.
 *
 * Contains the core payment processing logic that both services need.
 *
 * @author Mahmoud
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessingService {

    private final InstallmentScheduleRepository installmentScheduleRepository;
    private final ContractRepository contractRepository;
    private final CapitalService capitalService;
    private final ProfitProcessingService profitProcessingService;
    private final PaymentReminderService reminderService;

    /**
     * Process installment-specific payment logic:
     * - Update schedule status
     * - Return capital (principal portion)
     * - Process profit (gross - will be netted at month-end)
     * - Update contract progress
     *
     * Note: This method does NOT handle overpayment - that should be handled by the caller
     * to avoid circular dependency issues.
     *
     * @param schedule The installment schedule being paid
     * @param contract The contract associated with the schedule
     * @param payment The payment entity
     * @param finalDiscount The discount amount applied
     * @param currentUser The user processing the payment
     * @return The overpayment amount (if any) that needs to be applied to next schedules
     */
    @Transactional
    public BigDecimal processInstallmentPayment(InstallmentSchedule schedule, Contract contract,
                                                Payment payment, BigDecimal finalDiscount, User currentUser) {

        // Calculate amounts
        BigDecimal actualPayment = getActualPayment(schedule, payment.getNetAmount(), finalDiscount);

        //   A. UPDATE SCHEDULE
        updateSchedulePayment(schedule, actualPayment, payment.getActualPaymentDate(), finalDiscount);

        //   B. CALCULATE PRINCIPAL & PROFIT PORTIONS
        BigDecimal[] portions = calculatePrincipalAndProfitPortions(schedule, actualPayment);
        BigDecimal principalPaid = portions[0];
        BigDecimal profitPaid = portions[1];

        log.info("Payment breakdown: total={}, principal={}, profit={}",
                actualPayment, principalPaid, profitPaid);

        //   C. RETURN CAPITAL (IMMEDIATE)
        // Principal portion frees up locked capital
        //  TODO CHECK
        if (principalPaid.compareTo(BigDecimal.ZERO) > 0 && contract.getPartner() != null) {
            capitalService.returnCapitalFromPayment(contract, principalPaid, payment.getId(), currentUser);
        }

        // D. RECORD GROSS PROFIT
        // This records GROSS profit. Net profit calculation happens at MONTH-END
        // after all expenses are collected for the month
        if (profitPaid.compareTo(BigDecimal.ZERO) > 0) {
            profitProcessingService.processProportionalInstallmentProfit(
                    schedule, actualPayment, payment.getActualPaymentDate(), currentUser);
        }

        //   E. UPDATE CONTRACT
        updateContractProgress(contract);

        //   F. MARK REMINDERS COMPLETED
        reminderService.markRemindersCompleted(schedule.getId());

        //   G. CALCULATE OVERPAYMENT (return to caller for handling)
        BigDecimal overpayment = payment.getNetAmount().subtract(actualPayment);

        log.info("Processed installment payment for schedule {}. Actual: {}, Overpayment: {}",
                schedule.getId(), actualPayment, overpayment);

        return overpayment;
    }

    /**
     * Calculate the actual payment amount considering discount and already paid amount
     */
    public BigDecimal getActualPayment(InstallmentSchedule schedule, BigDecimal netAmount, BigDecimal finalDiscount) {
        // TODO check discount logic should user know final discont before apply
         finalDiscount = getSafeDecimal(finalDiscount);
        netAmount = getSafeDecimal(netAmount);
        BigDecimal currentPaid = schedule.getPaidAmount() != null ? schedule.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal amountDue = schedule.getAmount().subtract(finalDiscount).subtract(currentPaid);

        return netAmount.min(amountDue);
    }

    // Helper to safely get BigDecimal values (treat null as zero)
    public static BigDecimal getSafeDecimal(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value;
    }

    /**
     * Update schedule payment information with principal and profit tracking
     */
    private void updateSchedulePayment(InstallmentSchedule schedule, BigDecimal actualPayment,
                                       LocalDate paidDate, BigDecimal discount) {

        BigDecimal currentPaid = schedule.getPaidAmount() != null ? schedule.getPaidAmount() : BigDecimal.ZERO;
        schedule.setPaidAmount(currentPaid.add(actualPayment));
        schedule.setPaidDate(paidDate);

        if (discount != null && discount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal currentDiscount = schedule.getDiscountApplied() != null
                    ? schedule.getDiscountApplied() : BigDecimal.ZERO;
            schedule.setDiscountApplied(currentDiscount.add(discount));
        }

        // Calculate and track principal and profit portions
        BigDecimal[] portions = calculatePrincipalAndProfitPortions(schedule, actualPayment);
        BigDecimal principalPaid = portions[0];
        BigDecimal profitPaid = portions[1];

        // Update principal and profit paid tracking
        BigDecimal currentPrincipalPaid = schedule.getPrincipalPaid() != null
                ? schedule.getPrincipalPaid() : BigDecimal.ZERO;
        BigDecimal currentProfitPaid = schedule.getProfitPaid() != null
                ? schedule.getProfitPaid() : BigDecimal.ZERO;

        schedule.setPrincipalPaid(currentPrincipalPaid.add(principalPaid));
        schedule.setProfitPaid(currentProfitPaid.add(profitPaid));

        // Determine status
        BigDecimal totalDiscount = schedule.getDiscountApplied() != null
                ? schedule.getDiscountApplied() : BigDecimal.ZERO;
        boolean isFullyPaid = schedule.getPaidAmount().compareTo(
                schedule.getAmount().subtract(totalDiscount)) >= 0;

        if (isFullyPaid) {
            schedule.setStatus(PaymentStatus.PAID);
        } else if (schedule.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            schedule.setStatus(PaymentStatus.PARTIALLY_PAID);
        }

        log.info("Updated schedule {} status to {} - Principal paid: {}, Profit paid: {}",
                schedule.getId(), schedule.getStatus(), principalPaid, profitPaid);

        installmentScheduleRepository.save(schedule);
    }

    /**
     * Calculate principal and profit portions of payment
     * Returns array: [principalPaid, profitPaid]
     */
    public BigDecimal[] calculatePrincipalAndProfitPortions(InstallmentSchedule schedule, BigDecimal paidAmount) {

        BigDecimal totalAmount = schedule.getAmount();
        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO};
        }

        // Calculate proportional amounts
        BigDecimal principalAmount = schedule.getPrincipalAmount() != null ? schedule.getPrincipalAmount() : BigDecimal.ZERO;
        BigDecimal principalRatio = principalAmount.divide(totalAmount, 10, RoundingMode.HALF_UP);
        BigDecimal principalPaid = paidAmount.multiply(principalRatio).setScale(2, RoundingMode.HALF_UP);

        BigDecimal profitPaid = paidAmount.subtract(principalPaid);

        return new BigDecimal[]{principalPaid, profitPaid};
    }

    /**
     * Update contract remaining amount and check completion
     */
    public void updateContractProgress(Contract contract) {
        // Recalculate remaining amount from unpaid schedules
        List<InstallmentSchedule> schedules = installmentScheduleRepository
                .findByContractIdOrderBySequenceNumberAsc(contract.getId());

        BigDecimal totalRemaining = schedules.stream()
                .filter(s -> s.getStatus() != PaymentStatus.PAID
                        && s.getStatus() != PaymentStatus.CANCELLED)
                .map(s -> {
                    BigDecimal amount = s.getAmount();
                    BigDecimal paid = s.getPaidAmount() != null ? s.getPaidAmount() : BigDecimal.ZERO;
                    BigDecimal discount = s.getDiscountApplied() != null ? s.getDiscountApplied() : BigDecimal.ZERO;
                    return amount.subtract(paid).subtract(discount);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        contract.setRemainingAmount(totalRemaining);

        // Check if contract is completed
        boolean allPaid = schedules.stream()
                .allMatch(s -> s.getStatus() == PaymentStatus.PAID
                        || s.getStatus() == PaymentStatus.CANCELLED);

        if (allPaid && totalRemaining.compareTo(BigDecimal.ZERO) <= 0) {
            contract.setStatus(ContractStatus.COMPLETED);
            contract.setCompletionDate(LocalDate.now());
            log.info("Contract {} completed!", contract.getId());
        }

        contractRepository.save(contract);
    }
}

