package com.mahmoud.maalflow.modules.installments.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@AllArgsConstructor
@Data
public class ScheduleParameters {

    private final int months;
    private final BigDecimal monthlyAmount;

}
