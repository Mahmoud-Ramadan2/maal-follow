package com.mahmoud.maalflow.modules.installments.schedule.dto;

import com.mahmoud.maalflow.modules.installments.contract.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleReconciliationResponse {

    private Long scheduleId;
    private Long contractId;
    private String contractNumber;
    private Integer sequenceNumber;
    private PaymentStatus status;
    private LocalDate dueDate;
    private LocalDate paidDate;
    private BigDecimal amount;
    private BigDecimal originalAmount;
    private BigDecimal principalAmount;
    private BigDecimal profitAmount;
    private BigDecimal paidAmount;
    private BigDecimal discountApplied;
    private BigDecimal principalPaid;
    private BigDecimal profitPaid;
    private Integer paymentCount;
    private BigDecimal totalPaymentAmount;
    private BigDecimal capitalReturned;
    private BigDecimal profitRecorded;
    private BigDecimal remainingDue;
    private List<Long> paymentIds;
}

