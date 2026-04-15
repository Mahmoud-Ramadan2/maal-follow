package com.mahmoud.maalflow.modules.installments.partner.dto;

import com.mahmoud.maalflow.modules.installments.partner.enums.PartnershipType;
import com.mahmoud.maalflow.modules.installments.partner.enums.PartnerStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PartnerResponse {

    private Long id;
    private String name;
    private String phone;
    private String address;
    private PartnershipType partnershipType;
    private BigDecimal sharePercentage;
    private PartnerStatus status;
    private LocalDate investmentStartDate;
    private String profitCalculationStartMonth;
    private BigDecimal totalInvestment;
    private BigDecimal totalWithdrawals;
    private BigDecimal currentBalance;
    private Boolean profitSharingActive;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdByName;
}