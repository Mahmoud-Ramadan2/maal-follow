package com.mahmoud.maalflow.modules.installments.schedule.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.modules.installments.schedule.dto.ScheduleParameters;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.mahmoud.maalflow.modules.shared.constants.AppConstants.MINIMUM_INSTALLMENT;
import static com.mahmoud.maalflow.modules.shared.constants.AppConstants.ROUNDING_UNIT;


/**
 *  This component encapsulates the business rules and logic for determining the installment schedule parameters, such as the number of months and monthly installment amount, based on the total contract amount and user inputs. It ensures that the calculated schedule adheres to the defined policies,
 *  such as minimum installment amounts and rounding rules.
 */
@Component
public class ScheduleGenerationPolicy {


    /**
     * Calculate collection parameters (number of months and monthly amount)
     */
    public ScheduleParameters calculateScheduleParameters(
            BigDecimal totalAmount,
            Integer numberOfMonths,
            BigDecimal monthlyAmount,
            Integer contractMonths) {

        // Case 1: User provides both months and amount - validate they match
        if (numberOfMonths != null && monthlyAmount != null) {
            BigDecimal rounded = roundToMultipleOf50(monthlyAmount);
            validateMonthsAndAmount(totalAmount, numberOfMonths, rounded);
            return new ScheduleParameters(numberOfMonths, rounded);
        }



        // Case 2: User provides monthly amount - calculate number of months
        if (monthlyAmount != null && monthlyAmount.compareTo(BigDecimal.ZERO) > 0) {
             BigDecimal roundedAmount = roundToMultipleOf50(monthlyAmount);
            int calculatedMonths = calculateMonthsFromAmount(totalAmount, roundedAmount);
            return new ScheduleParameters(calculatedMonths, roundedAmount);
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
     * Round amount to nearest multiple of 50 (round down)
     */
    public BigDecimal roundToMultipleOf50(BigDecimal amount) {
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
     * Validate that provided months and amount are compatible
     */
    private void validateMonthsAndAmount(BigDecimal totalAmount, int months, BigDecimal monthlyAmount) {
        if (months <= 0) {
            throw new BusinessException("messages.contract.invalidMonths");
        }

        BigDecimal maxTotal = monthlyAmount.multiply(BigDecimal.valueOf(months));
        BigDecimal difference = maxTotal.subtract(totalAmount).abs();

        if (difference.compareTo(BigDecimal.ONE) > 0) {
            throw new BusinessException("messages.contract.monthsAmountMismatch");
        }
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



}
