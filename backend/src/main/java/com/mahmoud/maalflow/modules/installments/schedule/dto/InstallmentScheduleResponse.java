package com.mahmoud.maalflow.modules.installments.schedule.dto;

import com.mahmoud.maalflow.modules.installments.contract.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InstallmentScheduleResponse {

    private Long id;
    private Long contractId;
    private  Integer sequenceNumber;
    private String customerName;
    private String customerPhone;
    private String profitMonth;
    private Boolean isFinalPayment;
    private BigDecimal amount;
    private BigDecimal originalAmount;
    private BigDecimal principalAmount;
    private BigDecimal profitAmount;
    private BigDecimal principalPaid;
    private BigDecimal profitPaid;
    private BigDecimal discountApplied;
    private BigDecimal paidAmount;
    private PaymentStatus status;
    private LocalDate dueDate;
    private LocalDate paidDate;
    private Long collectorId;
    private String collectorName;
    private String collectorRole;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

