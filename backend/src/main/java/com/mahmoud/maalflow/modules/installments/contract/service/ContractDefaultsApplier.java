package com.mahmoud.maalflow.modules.installments.contract.service;

import com.mahmoud.maalflow.modules.installments.contract.entity.Contract;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ContractDefaultsApplier {


    /**
     * Apply default values to contract fields if not provided
     */
    public void applyDefaults(Contract contract) {
        if (contract.getAdditionalCosts() == null)
            contract.setAdditionalCosts(BigDecimal.ZERO);

        if (contract.getAgreedPaymentDay() == null)
            contract.setAgreedPaymentDay(1);

        if (contract.getCashDiscountRate() == null)
            contract.setCashDiscountRate(BigDecimal.ZERO);

        if (contract.getEarlyPaymentDiscountRate() == null)
            contract.setEarlyPaymentDiscountRate(BigDecimal.ZERO);

        if (contract.getDownPayment() == null)
            contract.setDownPayment(BigDecimal.ZERO);

        if (contract.getFinalPrice() == null)
            contract.setFinalPrice(BigDecimal.ZERO);

    }


}
