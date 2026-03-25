package com.mahmoud.maalflow.modules.installments.capital.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for CapitalPool operations.
 * Represents the current state of the shared capital pool.
 *
 * @author Mahmoud
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CapitalPoolResponse {

    private Long id;
    private BigDecimal totalAmount;
    private BigDecimal availableAmount;
    private BigDecimal lockedAmount;
    private BigDecimal returnedAmount;
    private BigDecimal ownerContribution;
    private BigDecimal partnerContributions;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Calculated fields
    private BigDecimal ownerSharePercentage;
    private BigDecimal partnerSharePercentage;
    private BigDecimal utilizationPercentage;  // locked / total
}
