package com.mahmoud.maalflow.modules.installments.contract.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.modules.installments.contract.entity.Contract;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 *  Policy class responsible for calculating and validating the relationship between months and monthly amount for a contract.
 *  It ensures that if the user provides one of the two values (months or monthly amount
 */
@Component
@Slf4j
public class ContractTermCalculator {

    /**
     * Calculate months and monthlyAmount based on user input.
     * User can provide either months OR monthlyAmount, and the other will be calculated.
     * If both are provided, validate they match.
     * If neither is provided, throw an error.
     */
    public void calculateMonthsAndAmount(Contract contract, BigDecimal remainingAmount, Integer months, BigDecimal monthlyAmount) {
        if (remainingAmount != null && remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("messages.contract.remainingAmount.invalid");
        }

        // Case 1: Both months and monthlyAmount provided - validate they match
        if (months != null && monthlyAmount != null) {
            BigDecimal calculatedTotal = monthlyAmount.multiply(BigDecimal.valueOf(months));
            BigDecimal difference = calculatedTotal.subtract(remainingAmount).abs();

            // Allow small difference due to rounding (within 1% or 10 units)
            //  BigDecimal tolerance = remainingAmount.multiply(BigDecimal.valueOf(0.01)).max(BigDecimal.TEN);

//            if (difference.compareTo(tolerance) > 0) {
            if (difference.compareTo(BigDecimal.ONE) > 0) {
                log.warn("Months and monthlyAmount mismatch: months={}, monthlyAmount={}, total={}, remaining={}",
                        months, monthlyAmount, calculatedTotal, remainingAmount);
                throw new BusinessException("messages.contract.monthsAmountMismatch");
            }

            contract.setMonths(months);
            contract.setMonthlyAmount(monthlyAmount);
            return;
        }

        // Case 2: Only monthlyAmount provided - calculate months
        if (monthlyAmount != null) {
            if (monthlyAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("messages.contract.invalidMonthlyAmount");
            }
            if (monthlyAmount.compareTo(remainingAmount) > 0) {
                throw new BusinessException("messages.contract.monthlyAmountExceedsTotal");
            }

            int calculatedMonths = remainingAmount.divide(monthlyAmount, 0, RoundingMode.UP).intValue();
            contract.setMonths(calculatedMonths);
            contract.setMonthlyAmount(monthlyAmount);
            log.debug("Calculated months from monthlyAmount: monthlyAmount={}, calculatedMonths={}", monthlyAmount, calculatedMonths);
            return;
        }

        // Case 3: Only months provided - calculate monthlyAmount
        if (months != null) {
            if (months <= 0) {
                throw new BusinessException("messages.contract.invalidMonths");
            }

            BigDecimal calculatedAmount = remainingAmount.divide(
                    BigDecimal.valueOf(months),
                    2,
                    RoundingMode.HALF_UP
            );

            contract.setMonths(months);
            contract.setMonthlyAmount(calculatedAmount);
            log.debug("Calculated monthlyAmount from months: months={}, calculatedAmount={}", months, calculatedAmount);
            return;
        }

        // Case 4: Neither provided - error
        throw new BusinessException("messages.contract.monthsOrAmountRequired");
    }

}
