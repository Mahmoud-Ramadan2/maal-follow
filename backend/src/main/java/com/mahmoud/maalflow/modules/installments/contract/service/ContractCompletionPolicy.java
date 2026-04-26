package com.mahmoud.maalflow.modules.installments.contract.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.modules.installments.contract.entity.Contract;
import com.mahmoud.maalflow.modules.installments.contract.enums.ContractStatus;
import com.mahmoud.maalflow.modules.installments.schedule.repo.InstallmentScheduleRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 *  Policy class to check if a contract can be marked as
 *  completed based on its installment schedules and payment status.
 */
@Slf4j
@AllArgsConstructor
@Component
public class ContractCompletionPolicy {

    private final InstallmentScheduleRepository scheduleRepository;

    /**
     * Check if all schedules are paid and mark contract as completed
     */
    public void checkAndCompleteContract(Contract contract, ContractStatus requestedStatus) {
        Long pendingCount = scheduleRepository.countPendingByContractId(contract.getId());
        BigDecimal remainingAmount = contract.getRemainingAmount();
        BigDecimal totalPaid = contract.getTotalPaid()
                .add(contract.getTotalDiscount())
                .add(contract.getDownPayment());
        if (pendingCount == 0) {
            if (totalPaid.compareTo(contract.getFinalPrice()) >= 0
                    && remainingAmount.compareTo(BigDecimal.ZERO) == 0) {
                contract.setStatus(ContractStatus.COMPLETED);
                contract.setCompletionDate(LocalDate.now());
            } else {
                log.warn("Contract {} has no pending schedules but remaining amount is {} and total paid is {}. Not marking as completed.",
                        contract.getId(), remainingAmount, totalPaid);
                throw new BusinessException("messages.contract.notFullyPaid");
            }
        }
        else if (requestedStatus != null && requestedStatus == ContractStatus.COMPLETED) {
            log.warn("Contract {} has {} pending schedules, not marking as completed", contract.getId(), pendingCount);
            throw new BusinessException("messages.contract.hasUnpaidInstallments");
        }
    }

}
