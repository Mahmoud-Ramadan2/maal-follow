package com.mahmoud.maalflow.modules.installments.collection.dto;

import com.mahmoud.maalflow.modules.installments.collection.enums.RouteType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for updating route metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionRouteUpdateRequest {
    private String name;
    private String description;
    private RouteType routeType;
}

