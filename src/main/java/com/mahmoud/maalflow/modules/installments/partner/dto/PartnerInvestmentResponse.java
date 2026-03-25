package com.mahmoud.maalflow.modules.installments.partner.dto;

import com.mahmoud.maalflow.modules.installments.partner.enums.InvestmentType;
import com.mahmoud.maalflow.modules.installments.partner.enums.InvestmentStatus;
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
public class PartnerInvestmentResponse {

    private Long id;
    private String partnerName;
    private BigDecimal amount;
    private InvestmentType investmentType;
    private InvestmentStatus status;
    private LocalDateTime investedAt;
    private LocalDateTime returnedAt;
    private String notes;
}