package com.mahmoud.maalflow.modules.installments.contract.event;

public class ContractExpenseChangedEvent {
    private final Long contractId;

    public ContractExpenseChangedEvent(Long contractId) {
        this.contractId = contractId;
    }

    public Long getContractId() {
        return contractId;
    }
}
