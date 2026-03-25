package com.mahmoud.maalflow.modules.installments.vendor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author Mahmoud
 */
@Data
@AllArgsConstructor
public class VendorResponse {

    private String name;
    private String phone;
    private String address;
    private String notes;
    private List<PurchaseDTO> purchases;
}

