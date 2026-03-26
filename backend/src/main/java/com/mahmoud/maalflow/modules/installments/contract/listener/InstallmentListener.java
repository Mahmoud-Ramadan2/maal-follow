package com.mahmoud.maalflow.modules.installments.contract.listener;

import com.mahmoud.maalflow.modules.installments.contract.entity.Contract;
import com.mahmoud.maalflow.modules.installments.contract.entity.InstallmentSchedule;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author Mahoud
 */
public class InstallmentListener {

    /**
     * After an installment schedule is created, updated, or removed, recalculate the total paid and total discount for the associated contract.
     */
    @PostPersist
    @PostUpdate
    @PostRemove
    public void updateContractTotals(InstallmentSchedule schedule) {
        Contract contract = schedule.getContract();
        if (contract != null) {
            // Recalculate totals from all schedules
            BigDecimal paid = contract.getInstallmentSchedules().stream()
                    .map(InstallmentSchedule::getPaidAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal discount = contract.getInstallmentSchedules().stream()
                    .map(InstallmentSchedule::getDiscountApplied)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            contract.setTotalPaid(paid);
            contract.setTotalDiscount(discount);
        }
    }
}
