package com.mahmoud.maalflow.modules.installments.partner.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PartnerProfitConfigResponse {
    private Long id;
    private BigDecimal managementFeePercentage;
    private BigDecimal zakatPercentage;
    private Integer profitPaymentDay;
    private Integer newPartnerDelayMonths;
    private Boolean active;
    private String notes;
}

