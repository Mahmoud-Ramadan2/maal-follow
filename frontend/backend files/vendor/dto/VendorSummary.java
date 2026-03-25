package com.mahmoud.maalflow.modules.installments.vendor.dto;

import lombok.Value;

/**
 * @author Mahmoud
 */
@Value
public class VendorSummary {

     Long id;
     String name;
     String phone;
     String address;
     String notes;
}
