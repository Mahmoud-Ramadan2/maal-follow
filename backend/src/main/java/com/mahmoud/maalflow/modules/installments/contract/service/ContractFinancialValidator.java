package com.mahmoud.maalflow.modules.installments.contract.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.modules.installments.contract.entity.Contract;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 *  This component is responsible for validating the financial aspects of a contract,
 *  such as final price, down payment, original price, and agreed payment day.
 * */
@Component
public class ContractFinancialValidator {

    /**
     * Validate financial fields of the contract
     */
    public void validateFinancials(
            BigDecimal finalPrice,
            BigDecimal downPayment,
            BigDecimal originalPrice,
            Integer agreedPaymentDay
    ) {
        if (finalPrice == null || downPayment == null || originalPrice == null || agreedPaymentDay == null) {
            throw new BusinessException("messages.contract.invalidFinancialData");
        }

        if (downPayment.compareTo(finalPrice) > 0) {
            throw new BusinessException("messages.contract.downPayment.invalid");
        }

        if (finalPrice.compareTo(originalPrice) < 0) {
            throw new BusinessException("messages.contract.finalPrice.lessThanOrignalPrice");
        }

        if (agreedPaymentDay < 1 || agreedPaymentDay > 31) {
            throw new BusinessException("messages.contract.agreedPaymentDay.invalid");

        }
    }

}
