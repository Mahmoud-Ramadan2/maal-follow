package com.mahmoud.maalflow.modules.installments.contract.listener;

import com.mahmoud.maalflow.modules.installments.contract.entity.Contract;
import com.mahmoud.maalflow.modules.installments.contract.event.ContractExpenseChangedEvent;
import com.mahmoud.maalflow.modules.installments.contract.repo.ContractExpenseRepository;
import com.mahmoud.maalflow.modules.installments.contract.repo.ContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class ContractExpenseTotalsListener {

    private final ContractRepository contractRepository;
    private final ContractExpenseRepository expenseRepository;



    @EventListener
    @Transactional
    public void onContractExpenseChanged(ContractExpenseChangedEvent event) {
        Long contractId = event.getContractId();
        if (contractId == null) return;

        Contract contract = contractRepository.findById(contractId).orElse(null);
        if (contract == null) return;

        BigDecimal totalExpenses = expenseRepository.getTotalExpensesByContractId(contractId);
        if (totalExpenses == null) totalExpenses = BigDecimal.ZERO;

        BigDecimal profit = contract.getProfitAmount() == null ? BigDecimal.ZERO : contract.getProfitAmount();
        BigDecimal netProfit = profit.subtract(totalExpenses).setScale(2, RoundingMode.HALF_UP);

        contract.setTotalExpenses(totalExpenses);
        contract.setNetProfit(netProfit);
        contractRepository.save(contract);
    }
}
