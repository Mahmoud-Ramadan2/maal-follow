package com.mahmoud.maalflow.modules.installments.collection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request payload for reordering route items.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionRouteReorderRequest {
    private List<Long> itemIds;
    private Boolean autoOptimize;
}

