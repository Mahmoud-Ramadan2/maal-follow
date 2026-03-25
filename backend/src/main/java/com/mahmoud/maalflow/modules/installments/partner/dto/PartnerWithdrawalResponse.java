package com.mahmoud.maalflow.modules.installments.partner.dto;

import com.mahmoud.maalflow.modules.installments.partner.enums.WithdrawalStatus;
import com.mahmoud.maalflow.modules.installments.partner.enums.WithdrawalType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PartnerWithdrawalResponse {

    private Long id;
    private String partnerName;
    private BigDecimal amount;
    private BigDecimal principalAmount;
    private BigDecimal profitAmount;
    private WithdrawalType withdrawalType;
    private WithdrawalStatus status;
    private String requestReason;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime processedAt;
    private String notes;
    private String processedByName;
    private String approvedByName;
}