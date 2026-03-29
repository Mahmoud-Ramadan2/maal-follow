package com.mahmoud.maalflow.modules.installments.contract.service;

import com.mahmoud.maalflow.exception.BusinessException;
import com.mahmoud.maalflow.modules.installments.contract.entity.Contract;
import com.mahmoud.maalflow.modules.installments.contract.enums.ContractStatus;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 *  Policy class to validate contract status transitions based on defined business rules.
 *  It ensures that only valid status changes are allowed and enforces completion rules when transitioning to COMPLETED.
 */
@Component
@AllArgsConstructor
public class ContractStatusPolicy {
    private final ContractCompletionPolicy contractCompletionPolicy;


    /**
     *  Validate if the requested status transition is allowed based on the current status of the contract.
     * @param current
     * @param requested
     * @param contract
     */
    public void validateStatusTransition(ContractStatus current, ContractStatus requested, Contract contract) {
//TODO      Define valid status transitions:
        if (requested == null || current == requested) {
            return; // no change
        }

        if (requested == ContractStatus.COMPLETED) {
            contractCompletionPolicy.checkAndCompleteContract(contract);
            return;
        }

        switch (current) {

            case ACTIVE:
                // ACTIVE →  CANCELLED or LATE allowed
                if (requested == ContractStatus.CANCELLED || requested == ContractStatus.LATE) {
                    return;
                }
                break;

            case LATE:
                // LATE → ACTIVE or CANCELLED allowed
                // LATE is final → cannot change
                if (requested == ContractStatus.LATE || requested == ContractStatus.ACTIVE) {
                    return;
                }
                break;
            case COMPLETED:
                // COMPLETED is final → cannot reopen or cancel
                if (requested == ContractStatus.COMPLETED) {
                    return;
                }
                break;

            case CANCELLED:
                // CANCELLED is final → cannot change
                if (requested == ContractStatus.CANCELLED) {
                    return;
                }
                break;
        }

        throw new BusinessException("messages.contract.invalidStatusTransition");
    }

}
