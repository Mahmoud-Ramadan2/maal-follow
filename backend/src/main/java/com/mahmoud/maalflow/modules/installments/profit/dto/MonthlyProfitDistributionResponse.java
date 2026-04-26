package com.mahmoud.maalflow.modules.installments.profit.dto;

import com.mahmoud.maalflow.modules.installments.profit.enums.ProfitDistributionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class MonthlyProfitDistributionResponse {
    private Long id;
    private String monthYear;
    private BigDecimal totalProfit;
    private BigDecimal managementFeePercentage;
    private BigDecimal zakatPercentage;
    private BigDecimal managementFeeAmount;
    private BigDecimal zakatAmount;
    private BigDecimal contractExpensesAmount;
    private BigDecimal distributableProfit;
    private BigDecimal ownerProfit;
    private BigDecimal partnersTotalProfit;
    private ProfitDistributionStatus status;
    private String calculationNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

