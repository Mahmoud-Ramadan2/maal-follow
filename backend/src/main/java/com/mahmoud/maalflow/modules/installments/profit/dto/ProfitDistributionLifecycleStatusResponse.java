package com.mahmoud.maalflow.modules.installments.profit.dto;

import com.mahmoud.maalflow.modules.installments.profit.enums.ProfitDistributionStatus;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProfitDistributionLifecycleStatusResponse {
    Long distributionId;
    String monthYear;
    ProfitDistributionStatus status;
}

