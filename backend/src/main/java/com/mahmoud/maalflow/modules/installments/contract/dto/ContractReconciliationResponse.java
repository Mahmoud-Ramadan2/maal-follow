package com.mahmoud.maalflow.modules.installments.contract.dto;

import com.mahmoud.maalflow.modules.installments.contract.enums.ContractStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractReconciliationResponse {

    private Long contractId;
    private String contractNumber;
    private ContractStatus status;
    private Long customerId;
    private String customerName;
    private Integer totalSchedules;
    private Integer paidSchedules;
    private Integer partiallyPaidSchedules;
    private Integer pendingSchedules;
    private Integer cancelledSchedules;
    private BigDecimal totalAmount;
    private BigDecimal storedRemainingAmount;
    private BigDecimal computedTotalPaid;
    private BigDecimal computedTotalDiscount;
    private BigDecimal computedRemainingDue;
    private BigDecimal totalExpenses;
    private BigDecimal capitalAllocated;
    private BigDecimal capitalReturned;
    private BigDecimal profitRecorded;
    private BigDecimal netProfit;
    private LocalDate startDate;
    private LocalDate completionDate;
}

