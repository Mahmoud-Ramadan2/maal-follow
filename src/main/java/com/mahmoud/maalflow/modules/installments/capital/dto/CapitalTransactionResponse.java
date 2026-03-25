package com.mahmoud.maalflow.modules.installments.capital.dto;

import com.mahmoud.maalflow.modules.installments.capital.enums.CapitalTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for CapitalTransaction operations.
 * Includes complete before/after balance audit trail.
 *
 * @author Mahmoud
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CapitalTransactionResponse {

    private Long id;
    private Long capitalPoolId;
    private CapitalTransactionType transactionType;
    private BigDecimal amount;

    // Before/after balance audit trail
    private BigDecimal availableBefore;
    private BigDecimal availableAfter;
    private BigDecimal lockedBefore;
    private BigDecimal lockedAfter;

    // References
    private String referenceType;
    private Long referenceId;
    private Long contractId;
    private Long paymentId;
    private Long partnerId;
    private String partnerName;

    private String description;
    private LocalDate transactionDate;
    private LocalDateTime createdAt;
    private String createdByUsername;
}
