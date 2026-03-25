package com.mahmoud.maalflow.modules.installments.partner.dto;

import com.mahmoud.maalflow.modules.installments.partner.enums.CommissionStatus;
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
public class PartnerProfitSharingResponse {

    private Long id;
    private String partnerName;
    private BigDecimal amount;
    private BigDecimal sharePercentage;
    private CommissionStatus status;
    private LocalDateTime calculatedAt;
    private LocalDateTime paidAt;
    private String notes;
    private String contractCustomerName;
    private String paymentDetails;
}