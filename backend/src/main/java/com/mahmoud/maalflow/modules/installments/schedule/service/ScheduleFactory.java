package com.mahmoud.maalflow.modules.installments.schedule.service;

import com.mahmoud.maalflow.modules.installments.contract.entity.Contract;
import com.mahmoud.maalflow.modules.installments.contract.enums.PaymentStatus;
import com.mahmoud.maalflow.modules.installments.schedule.entity.InstallmentSchedule;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.mahmoud.maalflow.modules.shared.constants.AppConstants.MINIMUM_INSTALLMENT;
import static com.mahmoud.maalflow.modules.shared.constants.AppConstants.MONTH_FORMAT;

/**
 *  This component is responsible for generating installment schedules based on the contract details and financials.
 *  It calculates the installment amounts, due dates, and distributes the principal and profit amounts proportion
 */
@Component
@AllArgsConstructor
public class ScheduleFactory {
    private final ScheduleGenerationPolicy scheduleGenerationPolicy;



    /**
     * Create list of installment schedules
     */
    public List<InstallmentSchedule> createScheduleList(
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
            roundedMonthlyAmount = scheduleGenerationPolicy.roundToMultipleOf50(
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
}
