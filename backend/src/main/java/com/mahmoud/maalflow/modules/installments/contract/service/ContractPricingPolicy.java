package com.mahmoud.maalflow.modules.installments.contract.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.modules.installments.contract.entity.Contract;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;


/**
 *  This component is responsible for applying the pricing policy to a contract, including calculating the final price based on the original price and contract duration, and calculating the profit amount.
 *  It encapsulates the business rules related to contract pricing and profit calculation.
 */
@Component
public class ContractPricingPolicy {

    /**
     * Auto Calculate final price if not provided depending Months and purchase price
     */
//    private void setAndValidateFinalPrice(Contract contract, BigDecimal finalPrice, BigDecimal purchasePrice) {
//
//        // TODO validate final price business logic
//
//        if (finalPrice == null) {
//            finalPrice = BigDecimal.ZERO;
//        }
//
//        if (purchasePrice == null) {
//            purchasePrice = BigDecimal.ZERO;
//        }
//
//        // Auto Calculate final price if not provided depending Months and purchase price
//        if(finalPrice.compareTo(BigDecimal.ZERO) > 0 ) {
//            // The minimum final price should be at least 10% markup over purchase price
//            if (finalPrice.compareTo(purchasePrice) < 0) {
//                throw new BusinessException("messages.contract.finalPrice.lessThanPurchasePrice");
//            }
//             if (finalPrice.compareTo(purchasePrice.
//                    multiply(BigDecimal.valueOf(1.1))) < 0) {
//                throw new BusinessException("messages.contract.finalPrice.lessThan10PercentMarkup");
//            }
//            else {
//                 contract.setFinalPrice(finalPrice.setScale(2, RoundingMode.HALF_UP));
//                return;
//            }
//        }
//
//        BigDecimal multiplier;
//        if (contract.getMonths() <= 6) {
//            // 30% markup for up to 6 months
//            multiplier = BigDecimal.valueOf(1.3);
//        } else if (contract.getMonths() <= 12) {
//            // 40% markup for 7 to 12 months
//            multiplier = BigDecimal.valueOf(1.4);
//        } else {
//            // 50% markup for more than 12 months
//            multiplier = BigDecimal.valueOf(1.5);
//        }
//        BigDecimal computed = purchasePrice.multiply(multiplier)
//                .setScale(2, RoundingMode.HALF_UP);
//
//        contract.setFinalPrice(computed);
//    }
    public void applyFinalPrice(Contract contract, BigDecimal finalPrice, BigDecimal originalPrice) {

        // TODO validate final price business logic


        // Auto Calculate final price if not provided depending Months and original price
        if (finalPrice != null && finalPrice.compareTo(BigDecimal.ZERO) > 0) {
            // The minimum final price should be at least 10% markup over the original price
            if (finalPrice.compareTo(originalPrice.multiply(BigDecimal.valueOf(1.1))) < 0) {
                throw new BusinessException("messages.contract.finalPrice.lessThan10PercentMarkup");
            }
            else {
                contract.setFinalPrice(finalPrice.setScale(2, RoundingMode.HALF_UP));
                return;
            }
        }

        // cant complete if moths not provided in this case as its needed to calculate final price
        // but it if final price provided and right value months can be not provided and will be calculated after that
        if (contract.getMonths() == null || contract.getMonths() <= 0 || contract.getMonths() > 60) {
            throw new BusinessException("messages.contract.invalidMonths");
        }

        BigDecimal multiplier;
        if (contract.getMonths() <= 6) {
            // 30% markup for up to 6 months
            multiplier = BigDecimal.valueOf(1.3);
        } else if (contract.getMonths() <= 12) {
            // 40% markup for 7 to 12 months
            multiplier = BigDecimal.valueOf(1.4);
        } else {
            // 50% markup for more than 12 months
            multiplier = BigDecimal.valueOf(1.5);
        }
        BigDecimal computed = originalPrice.multiply(multiplier)
                .setScale(2, RoundingMode.HALF_UP);

        contract.setFinalPrice(computed);
    }

    /**
     * Calculate profit amount based on purchase price and final price
     */
    public BigDecimal calculateProfit(Contract contract) {
        // TODO validate profit calculation business logic
        // Profit = Final Price - Original Price (which includes purchase + additional costs)
        return contract.getFinalPrice().subtract(contract.getOriginalPrice());
    }

}
