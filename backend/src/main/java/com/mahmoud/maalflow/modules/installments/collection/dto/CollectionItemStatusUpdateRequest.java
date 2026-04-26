package com.mahmoud.maalflow.modules.installments.collection.dto;

import com.mahmoud.maalflow.modules.installments.collection.enums.CollectionItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request payload for updating route item status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionItemStatusUpdateRequest {
    private CollectionItemStatus status;
    private String notes;
    private BigDecimal collectedAmount;
}

