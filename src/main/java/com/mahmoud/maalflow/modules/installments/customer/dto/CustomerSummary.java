package com.mahmoud.maalflow.modules.installments.customer.dto;

import lombok.Value;

/**
 * @author Mahmoud
 */
@Value
public class CustomerSummary {

     String name;
     String phone;
     String address;
     String nationalId;
     String notes;
     String createdAt;
}
