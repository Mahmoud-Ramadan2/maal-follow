package com.mahmoud.maalflow.modules.installments.schedule.dto;

import java.math.BigDecimal;

public record MonthlyCollectionSummary(
        String month,
        BigDecimal expectedAmount,
        BigDecimal actualAmount,
        BigDecimal shortfall
) {}

