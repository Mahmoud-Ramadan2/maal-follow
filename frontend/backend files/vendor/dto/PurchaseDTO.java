package com.mahmoud.maalflow.modules.installments.vendor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@AllArgsConstructor
@Data
public class PurchaseDTO {
    private String productName;
    private BigDecimal buyPrice;
}
