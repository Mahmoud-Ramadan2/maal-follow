package com.mahmoud.maalflow.modules.installments.partner.dto;
import lombok.Builder;
import lombok.Value;
import java.math.BigDecimal;
import java.util.List;
@Value
@Builder
public class PayoutReconciliationResponse {
    Long payoutId;
    String payoutType;
    String payoutStatus;
    Long partnerId;
    BigDecimal payoutAmount;
    Long ledgerEntryId;
    String ledgerIdempotencyKey;
    BigDecimal ledgerAmount;
    String ledgerReferenceType;
    Long ledgerReferenceId;
    String ledgerDescription;
    Long capitalTransactionId;
    BigDecimal capitalAmount;
    String capitalReferenceType;
    Long capitalReferenceId;
    String capitalDescription;
    boolean ledgerMatched;
    boolean capitalMatched;
    boolean fullyReconciled;
    List<String> issues;
}