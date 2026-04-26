package com.mahmoud.maalflow.modules.installments.payment.dto;

import com.mahmoud.maalflow.modules.installments.payment.enums.PaymentMethod;
import com.mahmoud.maalflow.modules.installments.payment.enums.PaymentProcessingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentReconciliationResponse {

    private Long paymentId;
    private String idempotencyKey;
    private PaymentProcessingStatus status;
    private LocalDateTime paymentDate;
    private LocalDate actualPaymentDate;
    private PaymentMethod paymentMethod;
    private BigDecimal amount;
    private BigDecimal discountAmount;
    private BigDecimal netAmount;
    private Boolean isEarlyPayment;
    private Long scheduleId;
    private Integer scheduleSequenceNumber;
    private com.mahmoud.maalflow.modules.installments.contract.enums.PaymentStatus scheduleStatus;
    private Long contractId;
    private String contractNumber;
    private Long receivedById;
    private String receivedByName;
    private Long collectorId;
    private String collectorName;
    private BigDecimal capitalReturned;
    private Integer capitalTransactionCount;
    private List<Long> capitalTransactionIds;
    private BigDecimal ledgerAmount;
    private Integer ledgerEntryCount;
    private List<Long> ledgerEntryIds;
    private BigDecimal nonCapitalPortion;
}

