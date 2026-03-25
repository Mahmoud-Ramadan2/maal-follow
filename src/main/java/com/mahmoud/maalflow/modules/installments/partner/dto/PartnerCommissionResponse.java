package com.mahmoud.maalflow.modules.installments.partner.dto;

import com.mahmoud.maalflow.modules.installments.partner.enums.CommissionStatus;
import com.mahmoud.maalflow.modules.installments.partner.enums.CommissionType;
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
public class PartnerCommissionResponse {

    private Long id;
    private String partnerName;
    private BigDecimal commissionAmount;
    private CommissionType commissionType;
    private CommissionStatus status;
    private LocalDateTime calculatedAt;
    private LocalDateTime paidAt;
    private String notes;
    private String purchaseProductName;
    private String contractCustomerName;
}