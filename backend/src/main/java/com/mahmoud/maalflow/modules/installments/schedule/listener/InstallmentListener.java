package com.mahmoud.maalflow.modules.installments.schedule.listener;

import com.mahmoud.maalflow.modules.installments.contract.entity.Contract;
import com.mahmoud.maalflow.modules.installments.schedule.entity.InstallmentSchedule;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;

import java.math.BigDecimal;

/**
 *  Listener class to handle updates to the Contract's total paid, total discount and remaining amount
 *  whenever an InstallmentSchedule is created, updated, or removed.
 * @author Mahoud
 */
public class InstallmentListener {

    /**
     * After an installment collection is created, updated, or removed,
     * recalculate the total paid, total discount and remaining amount for the associated contract.
     */
    @PostPersist
    @PostUpdate
    @PostRemove
    public void updateContractTotals(InstallmentSchedule schedule) {
        Contract contract = schedule.getContract();
        if (contract == null) {
            return;
        }

            // 1. Sum all paid amounts from the contract's list of installments
            BigDecimal totalPaid = contract.getInstallmentSchedules().stream()
                    .map(s -> s.getPaidAmount() != null ? s.getPaidAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 2. Sum all discount amounts
            BigDecimal totalDiscount = contract.getInstallmentSchedules().stream()
                    .map(s -> s.getDiscountApplied() != null ? s.getDiscountApplied() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 3. Update the parent Contract
            contract.setTotalPaid(totalPaid);
            contract.setTotalDiscount(totalDiscount);

            // 4. Update the remaining balance logic
            // remaining = (finalPrice - downPayment) - totalPaid - totalDiscount
            BigDecimal remaining = contract.getFinalPrice()
                    .subtract(contract.getDownPayment())
                    .subtract(totalPaid)
                    .subtract(totalDiscount);

            contract.setRemainingAmount(remaining);
    }
}
