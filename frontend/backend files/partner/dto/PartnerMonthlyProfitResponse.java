package com.mahmoud.maalflow.modules.installments.partner.dto;

import com.mahmoud.maalflow.modules.installments.partner.enums.ProfitStatus;
import com.mahmoud.maalflow.modules.installments.payment.enums.PaymentMethod;
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
public class PartnerMonthlyProfitResponse {

    private Long id;
    private String partnerName;
    private String profitDistributionMonth;
    private BigDecimal investmentAmount;
    private BigDecimal sharePercentage;
    private BigDecimal calculatedProfit;
    private ProfitStatus status;
    private LocalDate paymentDate;
    private PaymentMethod paymentMethod;
    private String notes;
    private LocalDateTime createdAt;
    private String paidByName;
}