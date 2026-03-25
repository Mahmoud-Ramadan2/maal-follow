package com.mahmoud.maalflow.modules.installments.purchase.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;
/**
 * @author Mahmoud
 *
 */

@Getter
@Builder
public class PurchaseStatistics {
    private long totalCount;
    private BigDecimal totalAmount;
    private BigDecimal avgAmount;
    private Map<String, Long> countByVendor;
    private Map<String, BigDecimal> amountByVendor;
}
