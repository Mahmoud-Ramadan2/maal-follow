package com.mahmoud.maalflow.modules.installments.contract.dto;

import com.mahmoud.maalflow.modules.installments.contract.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InstallmentScheduleResponse {

    private String contractNumber;
    private  Integer sequenceNumber;
    private String customerName;
    private String customerPhone;
    private String profitMonth;
    private Boolean isFinalPayment;
    private BigDecimal originalAmount;
    private BigDecimal principalAmount;
    private BigDecimal profitAmount;
    private LocalDate dueDate;
    private BigDecimal amount;
    private PaymentStatus status;
    private BigDecimal paidAmount;
    private LocalDate paidDate;
    private String collectorName;
    private String notes;
}

