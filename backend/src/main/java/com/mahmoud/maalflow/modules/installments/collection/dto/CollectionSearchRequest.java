package com.mahmoud.maalflow.modules.installments.collection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for searching candidate customers for collection routes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionSearchRequest {
    private String searchTerm;
    private String address;
    private Integer page;
    private Integer size;
}

