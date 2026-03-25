package com.mahmoud.maalflow.modules.installments.contract.enums;

/**
 * Status of installment contract.
 *
 * @author Mahmoud
 */
public enum ContractStatus {
    ACTIVE,
    COMPLETED,
    LATE,
    CANCELLED,
    ALL // for filtering purposes
    //TODO: Add BINDING ,other statuses
}

